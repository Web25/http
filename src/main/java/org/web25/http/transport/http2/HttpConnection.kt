package org.web25.http.transport.http2

import com.twitter.hpack.Decoder
import com.twitter.hpack.Encoder
import org.apache.commons.lang3.RandomUtils
import org.slf4j.LoggerFactory
import org.web25.http.Constants
import org.web25.http.HttpContext
import org.web25.http.drivers.push.PushRequest
import org.web25.http.drivers.server.ExecutorListener
import org.web25.http.drivers.server.HttpHandlerStack
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse
import org.web25.http.transport.http2.frames.GoAwayFrame
import org.web25.http.transport.http2.frames.SettingsFrame
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.net.Socket
import java.net.SocketException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by felix on 9/3/16.
 */
class HttpConnection @Throws(IOException::class)
constructor(socket: Socket, val localSettings: HttpSettings, private val executorListener: ExecutorListener, val context: HttpContext) {

    val remoteSettings: HttpSettings
    private val frameWriter: HttpFrameWriter
    private val frameReader: HttpFrameReader

    private var headerCompressionDecoder: AtomicReference<Decoder>? = null
    private var headerCompressionEncoder: AtomicReference<Encoder>? = null

    private val lastRemote = AtomicInteger(0)
    private val nextStreamIdentifier: AtomicInteger

    private val updateTime = AtomicLong(-1)

    private var state: State? = null

    var flowControlWindow: FlowControlWindow = FlowControlWindow(this)
        private set

    private val httpStreams: TreeMap<Int, HttpStream>

    private val frameQueue: ConcurrentLinkedQueue<HttpFrame>

    private val exclusiveHeaderLock = AtomicBoolean(false)
    private val lockHolder = AtomicInteger(-1)
    private var httpHandlerStack: HttpHandlerStack = HttpHandlerStack()

    init {
        this.remoteSettings = HttpSettings(HttpSettings.EndpointType.CLIENT)
        if (localSettings.endpointType == HttpSettings.EndpointType.CLIENT) {
            nextStreamIdentifier = AtomicInteger(1)
        } else if (localSettings.endpointType == HttpSettings.EndpointType.SERVER) {
            nextStreamIdentifier = AtomicInteger(2)
        } else {
            if (localSettings.isInitiator) {
                nextStreamIdentifier = AtomicInteger(1)
            } else {
                nextStreamIdentifier = AtomicInteger(2)
            }
        }
        this.frameWriter = HttpFrameWriter(socket.outputStream, this)
        this.frameReader = HttpFrameReader(socket.inputStream, this)
        this.httpStreams = TreeMap<Int, HttpStream>()
        this.frameQueue = ConcurrentLinkedQueue<HttpFrame>()
        //this.headerCompressionDecoder = new AtomicReference<>(new Decoder())
        //this.headerCompressionEncoder = new AtomicReference<>(new Encoder())
        this.state = State.ACTIVE
    }

    @Synchronized @Throws(IOException::class)
    fun enqueueFrame(frame: HttpFrame, httpStream: HttpStream?) {
        if (frame.type == Constants.Http20.FrameType.SETTINGS) {
            if (frame.flags.toInt() == 0) {
                updateTime.set(System.currentTimeMillis())
            }
        }
        if (frame.streamIdentifier == 0) {
            frameWriter.write(frame)
        } else if (frame.type == Constants.Http20.FrameType.DATA) {
            if (httpStream!!.flowControlWindow.checkOutgoing(frame.length)) {
                httpStream.flowControlWindow.decreaseRemote(frame.length)
                frameWriter.write(frame)
            } else {
                frameQueue.add(frame)
            }
        } else {
            frameWriter.write(frame)
        }

    }

    fun openNewStream(): HttpStream {
        return HttpStream(this, nextStreamIdentifier.getAndAdd(2))
    }

    fun handler(httpHandlerStack: HttpHandlerStack) {
        this.httpHandlerStack = httpHandlerStack
    }

    val hpackDecoder: Decoder
        get() = headerCompressionDecoder!!.get()

    @Throws(IOException::class)
    fun ping() {
        val ping = HttpFrame(remoteSettings)
        ping.type = Constants.Http20.FrameType.PING
        ping.payload = RandomUtils.nextBytes(8)
        enqueueFrame(ping, null)
    }

    fun handle() {
        log.debug("Starting handling of connection")
        var frame: HttpFrame
        try {
            frame = frameReader.read()
        } catch (e: IOException) {
            log.warn("Error while reading handshake setting frame!")
            return
        }

        if (frame.type != Constants.Http20.FrameType.SETTINGS) {
            log.warn("First frame wasn't a settings frame. Protocol violation")
            return
        }
        try {
            remoteSettings.apply(SettingsFrame.from(frame))
            val settingsFrame = SettingsFrame(remoteSettings)
            settingsFrame.isAck = true
            enqueueFrame(settingsFrame, null)
        } catch (e: Http20Exception) {
            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
            return
        } catch (e: IOException) {
            log.error("Error while sending settings ack")
            return
        }

        this.headerCompressionDecoder = AtomicReference(Decoder(localSettings.maxHeaderListSize, localSettings.headerTableSize))
        this.headerCompressionEncoder = AtomicReference(Encoder(localSettings.headerTableSize))
        this.flowControlWindow = FlowControlWindow(this)
        var run = true
        while (run) {
            try {
                try {
                    frame = frameReader.read()
                } catch (e: HttpFrameException) {
                    log.warn("Error while reading http frame", e)
                    terminate(e.errorCode)
                    break
                }

                if (!performSanityChecks(frame)) {
                    break
                }
                synchronized(httpStreams) {
                    if (exclusiveHeaderLock.get() && frame.streamIdentifier != lockHolder.get()) {
                        terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                        run = false
                    }
                    if (frame.length > localSettings.maxFrameSize) {
                        log.debug("Received invalidly sized frame")
                        if (frame.type == Constants.Http20.FrameType.HEADERS ||
                                frame.type == Constants.Http20.FrameType.PUSH_PROMISE ||
                                frame.type == Constants.Http20.FrameType.CONTINUATION ||
                                frame.type == Constants.Http20.FrameType.SETTINGS ||
                                frame.streamIdentifier == 0) {
                            terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                        } else {
                            val rstFrame = HttpFrame(remoteSettings)
                            rstFrame.type = Constants.Http20.FrameType.RST_STREAM
                            rstFrame.streamIdentifier = frame.streamIdentifier
                            rstFrame.payload = HttpUtil.toByte(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                            enqueueFrame(rstFrame, httpStreams[frame.streamIdentifier])
                        }
                    } else if (frame.type >= 0xa) {
                        log.debug("Dropping unknown frame " + frame.toString())
                    } else if (frame.streamIdentifier == 0) {
                        if (frame.type == Constants.Http20.FrameType.SETTINGS) {
                            if (frame.length % 6 != 0) {
                                terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                            }
                            val settingsFrame = SettingsFrame.from(frame)
                            if (settingsFrame.isAck && frame.length != 0) {
                                terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                            } else if (settingsFrame.isAck) {
                                val diff = System.currentTimeMillis() - updateTime.getAndSet(-1)
                                if (diff > 100) {
                                    log.warn("Settings ack took some time...")
                                }
                            } else {
                                try {
                                    remoteSettings.apply(settingsFrame)
                                } catch (e: Http20Exception) {
                                    log.warn("Invalid settings", e)
                                    terminate(e.errorCode)
                                }

                                val sFrame = SettingsFrame(remoteSettings)
                                sFrame.isAck = true
                                enqueueFrame(sFrame, null)
                            }
                        } else if (frame.type == Constants.Http20.FrameType.WINDOW_UPDATE) {
                            try {
                                flowControlWindow.incremetRemote(HttpUtil.toInt(frame.payload))
                            } catch (e: Http20Exception) {
                                log.warn(e.message)
                                terminate(e.errorCode)
                            }

                        } else if (frame.type == Constants.Http20.FrameType.PING) {
                            if (frame.flags.toInt() == 1) {
                                log.debug("Ping acknowledged.")
                            } else {
                                if (frame.length != 8) {
                                    terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                                    run = false
                                }
                                val pong = HttpFrame(remoteSettings)
                                pong.type = Constants.Http20.FrameType.PING
                                pong.payload = frame.payload
                                pong.flags = 1.toShort()
                                enqueueFrame(pong, null)
                            }
                        } else {
                            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                            run = false
                        }
                    } else if (httpStreams.containsKey(frame.streamIdentifier)) {
                        try {
                            httpStreams[frame.streamIdentifier]?.handleFrame(frame)
                        } catch (e: Http20Exception) {
                            log.warn(e.message)
                            terminate(e.errorCode)
                            run = false
                        }

                    } else {
                        if (this@HttpConnection.state == State.CLOSE_PENDING) {
                            log.info("Connection is shutting down, dropping new incoming stream with id " + frame.streamIdentifier)
                        } else if (frame.type == Constants.Http20.FrameType.RST_STREAM) {
                            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR, "Protocol violation! A stream cannot be closed in IDLE state. Shutting down connection!")
                        } else if (frame.type == Constants.Http20.FrameType.DATA ||
                                frame.type == Constants.Http20.FrameType.WINDOW_UPDATE ||
                                frame.type == Constants.Http20.FrameType.CONTINUATION) {
                            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                        } else {
                            if (remoteSettings.endpointType == HttpSettings.EndpointType.CLIENT || remoteSettings.isInitiator) {
                                if (frame.streamIdentifier % 2 == 0) {
                                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                                }
                            }
                            val httpStream = HttpStream(this, frame.streamIdentifier)
                            lastRemote.set(frame.streamIdentifier)
                            httpStreams.put(frame.streamIdentifier, httpStream)
                            try {
                                httpStream.handleFrame(frame)
                            } catch (e: Http20Exception) {
                                log.warn(e.message)
                                terminate(e.errorCode)
                                run = false
                            }

                        }
                    }
                    Unit        //Just because i can, dunno why I have to put this there, but don't remove it!
                }
            } catch (e: SocketException) {
                log.debug("Socket closed")
                run = false
            } catch (e: IOException) {
                log.warn("Error during connection handling", e)
                run = false
            }

            if (state == State.CLOSE || state == State.FORCE_CLOSED) {
                run = false
            }
        }
    }

    private fun performSanityChecks(httpFrame: HttpFrame): Boolean {
        when (httpFrame.type) {
            Constants.Http20.FrameType.RST_STREAM -> {
                if (httpFrame.streamIdentifier == 0) {
                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                    return false
                }
                if (httpFrame.length != 4) {
                    terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                    return false
                }
            }
            Constants.Http20.FrameType.PRIORITY -> {
                if (httpFrame.streamIdentifier == 0) {
                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                    return false
                }
                if (httpFrame.length != 5) {
                    terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                    return false
                }
            }
            Constants.Http20.FrameType.DATA -> {
                if (httpFrame.streamIdentifier == 0) {
                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                    return false
                }
                if (httpFrame.flags.toInt() and 0x8 == 0x8) {
                    val padLength = (httpFrame.payload[0].toInt() and 0xff).toShort()
                    if (padLength >= httpFrame.length) {
                        terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                        return false
                    }
                }
            }
            Constants.Http20.FrameType.HEADERS -> {
                if (httpFrame.streamIdentifier == 0) {
                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                    return false
                }
                if (httpFrame.flags.toInt() and 0x8 == 0x8) {
                    val padLength = (httpFrame.payload[0].toInt() and 0xff).toShort()
                    var frameLength = httpFrame.length
                    if (httpFrame.flags.toInt() and 0x20 == 0x20) {
                        frameLength -= 5       //length of priority portion
                    }
                    if (padLength >= frameLength) {
                        terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                        return false
                    }
                }
            }
            Constants.Http20.FrameType.WINDOW_UPDATE -> {
                if (httpFrame.length != 4) {
                    terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
                    return false
                }
                if (httpFrame.streamIdentifier == 0) {
                    if (HttpUtil.toInt(httpFrame.payload) <= 0) {
                        terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                        return false
                    }
                }
            }
            Constants.Http20.FrameType.CONTINUATION -> {
                if (httpFrame.streamIdentifier == 0) {
                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR)
                    return false
                }
            }
        }
        return true
    }

    fun exclusiveHeaderLock(httpStream: HttpStream) {
        if (exclusiveHeaderLock.get()) {
            terminate(Constants.Http20.ErrorCodes.INTERNAL_ERROR)
            throw Http20Exception("Invalid state! Error 0x01")
        }
        exclusiveHeaderLock.set(true)
        lockHolder.set(httpStream.streamIdentifier)
    }

    fun releaseLock(httpStream: HttpStream) {
        if (!exclusiveHeaderLock.get()) {
            terminate(Constants.Http20.ErrorCodes.INTERNAL_ERROR)
            throw Http20Exception("Invalid state! Error 0x02")
        }
        if (lockHolder.get() == httpStream.streamIdentifier) {
            exclusiveHeaderLock.set(false)
        }
    }

    fun deferHandling(httpRequest: IncomingHttpRequest, httpResponse: OutgoingHttpResponse, httpStream: HttpStream) {
        executorListener.submit(DeferredRequestHandler(httpStream, httpHandlerStack, httpRequest, httpResponse))
    }

    val hpackEncoder: Encoder
        get() = headerCompressionEncoder!!.get()

    fun allocatePushStream(pushRequest: PushRequest): HttpStream {
        val httpStream = HttpStream(this, nextStreamIdentifier.getAndAdd(2))
        httpStream.preparePush(pushRequest)
        synchronized(httpStreams) {
            httpStreams.put(httpStream.streamIdentifier, httpStream)
        }
        return httpStream
    }

    inner class HttpConnectionReceiver : Runnable {

        override fun run() {
            handle()
        }
    }

    fun terminate(errorCode: Int, debugData: String) {
        terminate(errorCode, debugData.toByteArray())
    }

    @JvmOverloads fun terminate(errorCode: Int, debugData: ByteArray? = null) {
        /*try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        val goAwayFrame = GoAwayFrame(remoteSettings)
        goAwayFrame.errorCode = errorCode
        goAwayFrame.lastStreamId = lastRemote.get()
        goAwayFrame.debugData = debugData
        try {
            frameWriter.write(goAwayFrame)
        } catch (e: IOException) {
            log.warn("Could not write GOAWAY frame")
            this.state = State.FORCE_CLOSED
        }

        this.state = State.CLOSE_PENDING
    }

    enum class State {
        ACTIVE, CLOSE_PENDING, CLOSE, FORCE_CLOSED
    }

    private inner class DeferredRequestHandler(private val httpStream: HttpStream, private val handlerStack: HttpHandlerStack, private val httpRequest: IncomingHttpRequest, private val httpResponse: OutgoingHttpResponse) : Runnable {

        override fun run() {
            try {
                handlerStack.handle(httpRequest, httpResponse)
            } catch (t: Throwable) {
                log.warn("Error while handling http request", t)
                val byteArrayOutputStream = ByteArrayOutputStream()
                t.printStackTrace(PrintStream(byteArrayOutputStream))
                httpResponse.status(500)
                httpResponse.entity(byteArrayOutputStream.toByteArray())
                httpResponse.header("Content-Type", "text/plain")
            }

            httpStream.notfiyHandlingFinished()
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("HTTP/2.0")
    }
}
