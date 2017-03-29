package org.web25.http.transport.http2

import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import org.web25.http.Constants
import org.web25.http.drivers.DefaultIncomingHttpRequest
import org.web25.http.drivers.push.PushRequest
import org.web25.http.drivers.push.PushableHttpResponse
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.transport.http2.frames.DataFrame
import org.web25.http.transport.http2.frames.HeadersFrame
import org.web25.http.transport.http2.frames.PushPromiseFrame
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

/**
 * Created by felix on 9/3/16.
 */
class HttpStream(val httpConnection: HttpConnection, val streamIdentifier: Int) {
    val flowControlWindow: FlowControlWindow

    private var terminated: Boolean = false
    private var waitingForHeaders: Boolean = false
    private var endStream: Boolean = false
    private var regularHeaders: Boolean = false

    lateinit var httpRequest: IncomingHttpRequest
    lateinit var httpResponse: PushableHttpResponse

    var state: State? = null
    private set(value) {
        log.debug("Stream {} transitioning from {} to {}", streamIdentifier, field, value)
        field = value
    }

    private var headerBuffer: ByteArrayOutputStream? = null

    init {
        this.state = State.IDLE
        this.flowControlWindow = FlowControlWindow(parent = httpConnection.flowControlWindow, connection = httpConnection)
    }

    private val isClosed: Boolean
        @Contract(pure = true)
        get() = state != State.CLOSED

    @Throws(IOException::class)
    fun sendFrame(httpFrame: HttpFrame) {
        httpConnection.enqueueFrame(httpFrame, this)
    }

    fun handleFrame(httpFrame: HttpFrame) {
        if (httpFrame.streamIdentifier != streamIdentifier) {
            throw HttpFrameException("Frame was delivered to wrong stream!")
        }
        if (httpFrame.type == Constants.Http20.FrameType.PING) {
            httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
        } else if (httpFrame.type == Constants.Http20.FrameType.GOAWAY) {
            httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
        }
        if (waitingForHeaders && httpFrame.type != Constants.Http20.FrameType.CONTINUATION) {
            httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            return
        }
        if (httpFrame.type == Constants.Http20.FrameType.WINDOW_UPDATE) {
            val increment = HttpUtil.toInt(httpFrame.payload)
            if (increment <= 0) {
                log.warn("Stream error. WINDOW_UPDATE requires a value between 0 and 2^31. Terminating stream.")
                terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            } else {
                try {
                    flowControlWindow.incremetRemote(HttpUtil.toInt(httpFrame.payload))
                } catch (e: Http20Exception) {
                    log.warn(e.message)
                    terminate(e.errorCode)
                }

            }
            return
        }
        if (state == State.IDLE) {
            if (waitingForHeaders && httpFrame.type != Constants.Http20.FrameType.CONTINUATION) {
                throw Http20Exception("During header transmission no other frames than CONTINUATION are allowed", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
            if (httpFrame.type == Constants.Http20.FrameType.HEADERS) {
                this.httpRequest = DefaultIncomingHttpRequest(httpConnection.context)
                val headersFrame = HeadersFrame.from(httpFrame)
                if (headersFrame.isPriority && headersFrame.streamDependency == streamIdentifier) {
                    throw Http20Exception("Circular stream dependency detected $streamIdentifier -> $streamIdentifier", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                }
                if (headersFrame.isEndHeaders) {
                    try {
                        httpConnection.hpackDecoder.decode(ByteArrayInputStream(headersFrame.headerBlockFragment), { nameBytes, valueBytes, _ -> this.addHeader(nameBytes, valueBytes) })
                    } catch (e: IOException) {
                        log.warn("Error while decoding headers", e)
                        httpConnection.terminate(Constants.Http20.ErrorCodes.COMPRESION_ERROR)
                    }

                    if (headersFrame.isEndStream) {
                        state = State.HALF_CLOSED_REMOTE
                        handle()
                    } else {
                        state = State.OPEN
                    }
                } else {
                    headerBuffer = ByteArrayOutputStream()
                    try {
                        headerBuffer!!.write(headersFrame.headerBlockFragment)
                    } catch (e: IOException) {
                        log.warn("Could not buffer header fragment", e)
                    }

                    if (headersFrame.isEndStream)
                        endStream = true
                    this.waitingForHeaders = true
                    httpConnection.exclusiveHeaderLock(this)
                }
            } else if (httpFrame.type == Constants.Http20.FrameType.CONTINUATION) {
                if (!waitingForHeaders) {
                    httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                    return
                }
                if (headerBuffer == null) {
                    httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                } else {
                    try {
                        headerBuffer!!.write(httpFrame.payload)
                    } catch (e: IOException) {
                        log.warn("Could not buffer header fragment", e)
                    }

                }
                if (httpFrame.flags.toInt() and 0x4 == 0x4) {
                    try {
                        httpConnection.hpackDecoder.decode(ByteArrayInputStream(headerBuffer!!.toByteArray()), { nameBytes, valueBytes, _ -> this.addHeader(nameBytes, valueBytes) })
                    } catch (e: IOException) {
                        log.warn("Error while decoding headers", e)
                        httpConnection.terminate(Constants.Http20.ErrorCodes.COMPRESION_ERROR)
                    }

                    httpConnection.releaseLock(this)
                    if (endStream) {
                        state = State.HALF_CLOSED_REMOTE
                        handle()
                    } else {
                        state = State.OPEN
                    }
                }
            } else if (httpFrame.type == Constants.Http20.FrameType.PRIORITY) {

            } else {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
        } else if (state == State.RESERVED_LOCAL) {
            if (httpFrame.type == Constants.Http20.FrameType.RST_STREAM) {
                state = State.CLOSED
            } else if (httpFrame.type == Constants.Http20.FrameType.PRIORITY) {

            } else {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
        } else if (state == State.RESERVED_REMOTE) {
            if (httpFrame.type == Constants.Http20.FrameType.HEADERS) {
                state = State.HALF_CLOSED_REMOTE
            } else if (httpFrame.type == Constants.Http20.FrameType.RST_STREAM) {
                state = State.CLOSED
            } else if (httpFrame.type == Constants.Http20.FrameType.PRIORITY) {

            } else {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
        } else if (state == State.OPEN) {
            if (httpFrame.hasEndStreamFlag()) {
                if (httpFrame.type == Constants.Http20.FrameType.DATA) {
                    try {
                        val dataFrame = DataFrame.from(httpFrame)

                        httpRequest.appendBytes(dataFrame.data)
                    } catch (e: HttpFrameException) {
                        terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                    }

                } else if (httpFrame.type == Constants.Http20.FrameType.HEADERS) {
                    try {
                        val headersFrame = HeadersFrame.from(httpFrame)
                        if (headersFrame.isPriority && headersFrame.streamDependency == streamIdentifier) {
                            throw Http20Exception("Circular stream dependency detected $streamIdentifier -> $streamIdentifier", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                        }
                        if (!headersFrame.isEndStream) {
                            throw Http20Exception("Invalid header frame in state OPEN", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                        } else {
                            if (headersFrame.isEndHeaders) {
                                try {
                                    httpConnection.hpackDecoder.decode(ByteArrayInputStream(headersFrame.headerBlockFragment), { nameBytes, valueBytes, _ -> this.addHeader(nameBytes, valueBytes) })
                                } catch (e: IOException) {
                                    log.warn("Error while decoding headers", e)
                                    httpConnection.terminate(Constants.Http20.ErrorCodes.COMPRESION_ERROR)
                                }

                                state = State.HALF_CLOSED_REMOTE
                                handle()
                            } else {
                                waitingForHeaders = true
                                httpConnection.exclusiveHeaderLock(this)
                                try {
                                    headerBuffer!!.write(headersFrame.headerBlockFragment)
                                } catch (e: IOException) {
                                    log.warn("Could not buffer header block fragment")
                                }

                                endStream = true
                            }
                        }
                    } catch (e: HttpFrameException) {
                        httpConnection.terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                    }

                }
                if (httpFrame.flags.toInt() and 0x1 == 0x1) {
                    state = State.HALF_CLOSED_REMOTE
                    handle()
                }
            } else if (httpFrame.type == Constants.Http20.FrameType.RST_STREAM) {
                state = State.CLOSED
            } else if (httpFrame.type == Constants.Http20.FrameType.CONTINUATION) {
                if (!waitingForHeaders) {
                    httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                } else {
                    if (headerBuffer == null) {
                        httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                    } else {
                        try {
                            headerBuffer!!.write(httpFrame.payload)
                        } catch (e: IOException) {
                            log.warn("Could not buffer header fragment", e)
                        }

                    }
                    if (httpFrame.flags.toInt() and 0x4 == 0x4) {
                        try {
                            httpConnection.hpackDecoder.decode(ByteArrayInputStream(headerBuffer!!.toByteArray()), { nameBytes, valueBytes, _ -> this.addHeader(nameBytes, valueBytes) })
                        } catch (e: IOException) {
                            log.warn("Error while decoding headers", e)
                            httpConnection.terminate(Constants.Http20.ErrorCodes.COMPRESION_ERROR)
                        }

                        httpConnection.releaseLock(this)
                        if (endStream) {
                            state = State.HALF_CLOSED_REMOTE
                            handle()
                        } else {
                            state = State.OPEN
                        }
                    }
                }
            }
        } else if (state == State.HALF_CLOSED_LOCAL) {
            if (httpFrame.type == Constants.Http20.FrameType.RST_STREAM) {
                state = State.CLOSED
            }
        } else if (state == State.HALF_CLOSED_REMOTE) {
            if (httpFrame.type == Constants.Http20.FrameType.PRIORITY) {
                //TODO implement prioritization
            } else if (httpFrame.type == Constants.Http20.FrameType.RST_STREAM) {
                state = State.CLOSED
            } else {
                terminate(Constants.Http20.ErrorCodes.STREAM_CLOSED)
            }
        } else if (state == State.CLOSED) {
            if (httpFrame.type == Constants.Http20.FrameType.DATA) {
                flowControlWindow.decreaseLocal(httpFrame.length)
                terminate(Constants.Http20.ErrorCodes.STREAM_CLOSED)
            } else if (httpFrame.type == Constants.Http20.FrameType.PRIORITY) {
                //TODO implement prioritization
            } else if (httpFrame.type == Constants.Http20.FrameType.WINDOW_UPDATE) {
                log.debug("Ignoring window update on already closed stream")
            } else {
                if (!terminated) {
                    terminate(Constants.Http20.ErrorCodes.STREAM_CLOSED)
                }
            }
        }
    }

    private fun addHeader(nameBytes: ByteArray, valueBytes: ByteArray) {
        val name = String(nameBytes)
        val value = String(valueBytes)
        log.debug("{} -> {}", name, value)
        if (HttpUtil.containsUppercase(name)) {
            throw Http20Exception("Invalid header. Has uppercase characters", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
        }
        if (name.startsWith(":")) {
            if (regularHeaders) {
                throw Http20Exception("Pseudo headers need to be sent before regular headers", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
            if (httpRequest.hasHeader(name)) {
                throw Http20Exception("Duplicate definition of pseudo header " + name, Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
            if (name == ":method") {
                httpRequest.method(value)
            } else if (name == ":path") {
                httpRequest.path(value)
            } else if (name == ":authority") {
                if (!httpRequest.hasHeader("host")) {
                    httpRequest.header("host", value)
                }
            } else if (name == ":status") {
                throw Http20Exception("Header status not allowed for requests!", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            } else if (name != ":scheme") {
                throw Http20Exception("Invalid pseudo header " + name, Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
        } else {
            if (name == "connection") {
                throw Http20Exception("Header connection is not applicable for an HTTP/2.0 connection", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            } else if (name == "te" && value != "trailers") {
                throw Http20Exception("Transfer encodings other than trailers are not allowed for an HTTP/2.0 connection", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            } else if (name == "cookie") {
                if (value.contains(";")) {
                    val cookies = value.split(";".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                    for (cookie in cookies) {
                        httpRequest.cookies[cookie.substring(0, cookie.indexOf("="))] = cookie.substring(cookie.indexOf("=") + 1)
                    }
                } else {
                    httpRequest.cookies[value.substring(0, value.indexOf("="))] = value.substring(value.indexOf("=") + 1)
                }
            }
            regularHeaders = true
        }
        httpRequest.header(name, value)
    }

    private fun handle() {
        val httpRequest = httpRequest
        if (!(httpRequest.hasHeader(":method") && httpRequest.hasHeader(":scheme") && httpRequest.hasHeader(":path"))) {
            throw Http20Exception("Missing pseudo header field(s)", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
        }
        if (httpRequest.hasHeader("Content-Length")) {
            log.debug("Received {} byte(s), headers stated {} byte(s)", httpRequest.entityBytes().size, httpRequest.headers["Content-Length"].toInt())
            if (httpRequest.entityBytes().size != httpRequest.headers["Content-Length"].toInt()) {
                throw Http20Exception("Invalid content length", Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            }
        }
        val httpResponse = PushableHttpResponse(httpRequest)
        this.httpResponse = httpResponse
        httpConnection.deferHandling(httpRequest, httpResponse, this)
    }

    fun notfiyHandlingFinished() {
        val encoder = httpConnection.hpackEncoder
        synchronized(encoder) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            try {
                encoder.encodeHeader(byteArrayOutputStream, ":status".toByteArray(), httpResponse.statusCode().toString().toByteArray(), false)
                httpResponse.headers.forEach { name, value ->
                    try {
                        encoder.encodeHeader(byteArrayOutputStream, name.toLowerCase().toByteArray(), value.toByteArray(), false)
                    } catch (e: IOException) {
                        log.warn("Could not encode headers")
                    }
                }
                httpResponse.cookies.forEach { httpCookie ->
                    try {
                        encoder.encodeHeader(byteArrayOutputStream, "set-cookie".toByteArray(), httpCookie.toString().toByteArray(), false)
                    } catch (e: IOException) {
                        log.warn("Could not encode cookie")
                    }
                }
            } catch (e: IOException) {
                log.warn("Could not encode headers")
            }

            val headersFrame = HeadersFrame(httpConnection.remoteSettings)
            headersFrame.isEndHeaders = true
            headersFrame.streamIdentifier = this.streamIdentifier
            headersFrame.headerBlockFragment = byteArrayOutputStream.toByteArray()
            try {
                httpConnection.enqueueFrame(headersFrame, this)
            } catch (e: IOException) {
                log.warn("Could not respond to request", e)
            }

            byteArrayOutputStream.reset()
            val pushRequests = httpResponse.getPushRequests()
            for (pushRequest in pushRequests) {
                log.info("Pushing " + pushRequest.method().toUpperCase() + " " + pushRequest.path())
                val pushPromiseFrame = PushPromiseFrame(httpConnection.remoteSettings)
                val pushStream = httpConnection.allocatePushStream(pushRequest)
                pushPromiseFrame.promisedStreamId = pushStream.streamIdentifier
                try {
                    encoder.encodeHeader(byteArrayOutputStream, ":method".toByteArray(), pushRequest.method().toByteArray(), false)
                    encoder.encodeHeader(byteArrayOutputStream, ":path".toByteArray(), pushRequest.path().toByteArray(), false)
                    pushRequest.headers.forEach { name, value ->
                        try {
                            encoder.encodeHeader(byteArrayOutputStream, name.toLowerCase().toByteArray(), value.toByteArray(), false)
                        } catch (e: IOException) {
                            log.warn("Could not encode headers")
                        }
                    }

                } catch (e: IOException) {
                    log.warn("Could not prepare push promise", e)
                }

                pushPromiseFrame.headerBlockFragment = byteArrayOutputStream.toByteArray()
                pushPromiseFrame.isEndHeaders = true
                pushPromiseFrame.streamIdentifier = streamIdentifier
                try {
                    httpConnection.enqueueFrame(pushPromiseFrame, this)
                } catch (e: IOException) {
                    log.warn("Could not send push promise", e)
                    continue
                }

                httpConnection.deferHandling(pushRequest, pushStream.httpResponse, pushStream)
                byteArrayOutputStream.reset()
            }
            val response = httpResponse.responseBytes()
            if (response.size > httpConnection.remoteSettings.maxFrameSize) {
                var begin = 0
                val frameSize = httpConnection.remoteSettings.maxFrameSize
                var i = 1
                while (i <= Math.ceil(response.size.toDouble() / frameSize)) {
                    val chunk = Arrays.copyOfRange(response, begin, frameSize * i)
                    val dataFrame = DataFrame(httpConnection.remoteSettings)
                    dataFrame.data = chunk
                    dataFrame.streamIdentifier = this.streamIdentifier
                    if (begin + frameSize >= response.size) {
                        dataFrame.flags = 0x1.toShort()
                    }
                    try {
                        httpConnection.enqueueFrame(dataFrame, this)
                    } catch (e: IOException) {
                        log.warn("Could not respond to request", e)
                    }

                    begin = frameSize * i
                    i++
                }
            } else {
                val dataFrame = DataFrame(httpConnection.remoteSettings)
                dataFrame.data = httpResponse.responseBytes()
                dataFrame.streamIdentifier = this.streamIdentifier
                dataFrame.flags = 0x1.toShort()
                try {
                    httpConnection.enqueueFrame(dataFrame, this)
                } catch (e: IOException) {
                    log.warn("Could not respond to request", e)
                }

            }
            state = State.CLOSED
        }
    }

    fun preparePush(pushRequest: PushRequest) {
        state = State.RESERVED_LOCAL
        this.httpRequest = pushRequest
        this.httpResponse = PushableHttpResponse(pushRequest)
    }

    enum class State {
        IDLE, RESERVED_LOCAL, RESERVED_REMOTE, OPEN, HALF_CLOSED_REMOTE, HALF_CLOSED_LOCAL, CLOSED
    }

    fun terminate(errorCode: Int) {
        val frame = HttpFrame(httpConnection.remoteSettings)
        frame.streamIdentifier = streamIdentifier
        frame.type = Constants.Http20.FrameType.RST_STREAM
        frame.payload = HttpUtil.toByte(errorCode)
        try {
            httpConnection.enqueueFrame(frame, this)
        } catch (e: IOException) {
            log.warn("Could not terminate stream", e)
        }

        terminated = true
        state = State.CLOSED
    }

    companion object {

        private val log = LoggerFactory.getLogger("HTTP/2.0")
    }
}
