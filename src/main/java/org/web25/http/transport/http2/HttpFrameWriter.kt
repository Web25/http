package org.web25.http.transport.http2

import org.slf4j.LoggerFactory
import org.web25.http.Constants
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Created by felix on 9/3/16.
 */
class HttpFrameWriter(private val outputStream: OutputStream, private val httpConnection: HttpConnection) {

    @Throws(IOException::class)
    fun write(httpFrame: HttpFrame) {
        try {
            httpFrame.build()
        } catch (t: Throwable) {
            log.error("Error while building frame", t)
        }

        log.debug("Writing frame: " + httpFrame.toString())
        val outputBuffer = ByteArrayOutputStream(httpFrame.length + Constants.Http20.FRAME_HEADER_LENGTH)
        outputBuffer.write(HttpUtil.toByte(httpFrame.length), 1, 3)
        outputBuffer.write(HttpUtil.toByte(httpFrame.type), 1, 1)
        outputBuffer.write(HttpUtil.toByte(httpFrame.flags), 1, 1)
        outputBuffer.write(HttpUtil.toByte(httpFrame.streamIdentifier))
        outputBuffer.write(httpFrame.payload)
        outputBuffer.writeTo(this.outputStream)
    }

    companion object {

        private val log = LoggerFactory.getLogger("HTTP/2.0")
    }
}
