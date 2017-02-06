package org.web25.http.transport.http2

import org.slf4j.LoggerFactory
import org.web25.http.transport.http2.util.DebugInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Created by felix on 9/3/16.
 */
class HttpFrameReader(inputStream: InputStream, private val httpConnection: HttpConnection) {

    private val inputStream: InputStream

    init {
        this.inputStream = DebugInputStream(inputStream)
    }

    @Throws(IOException::class)
    fun read(): HttpFrame {
        val httpFrame = HttpFrame(httpConnection.localSettings)
        val header = ByteArray(9)
        inputStream.read(header, 0, 9)
        var buffer: ByteArray
        buffer = Arrays.copyOfRange(header, 0, 3)
        httpFrame.length = HttpUtil.toInt(buffer)
        buffer = Arrays.copyOfRange(header, 3, 4)
        httpFrame.type = HttpUtil.toShort(buffer)
        buffer = Arrays.copyOfRange(header, 4, 5)
        httpFrame.flags = HttpUtil.toShort(buffer)
        buffer = Arrays.copyOfRange(header, 5, 9)
        httpFrame.streamIdentifier = HttpUtil.toInt(buffer)
        buffer = ByteArray(httpFrame.length)
        inputStream.read(buffer, 0, buffer.size)
        httpFrame.payload = buffer
        log.debug("Incoming frame: " + httpFrame.toString())
        return httpFrame
    }

    companion object {

        private val log = LoggerFactory.getLogger("HTTP/2.0")
    }
}
