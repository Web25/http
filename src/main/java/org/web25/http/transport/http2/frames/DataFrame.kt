package org.web25.http.transport.http2.frames

import org.web25.http.Constants
import org.web25.http.transport.http2.HttpFrame
import org.web25.http.transport.http2.HttpFrameException
import org.web25.http.transport.http2.HttpSettings
import org.web25.http.transport.http2.HttpUtil

import java.nio.ByteBuffer

/**
 * Created by felix on 9/18/16.
 */
class DataFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
(settings: HttpSettings) : PaddedFrame(settings) {

    lateinit var data: ByteArray

    init {
        type = Constants.Http20.FrameType.DATA
    }

    override fun build() {
        var length = data.size
        if (isPadded) {
            length++  //padding adds one byte
            length += padLength.toInt()
        }
        val buffer = ByteBuffer.allocate(length)
        if (isPadded)
            buffer.put(HttpUtil.toByte(padLength), 1, 1)
        buffer.put(data)
        payload = buffer.array()
    }

    companion object {

        fun from(frame: HttpFrame): DataFrame {
            val dataFrame = DataFrame(frame.settings)
            dataFrame.flags = frame.flags
            dataFrame.streamIdentifier = frame.streamIdentifier
            val byteBuffer = ByteBuffer.wrap(frame.payload)
            if (dataFrame.isPadded) {
                dataFrame.padLength = (byteBuffer.get().toInt() and 0xff).toShort()
            }
            val data = ByteArray(byteBuffer.remaining() - dataFrame.padLength)
            byteBuffer.get(data)
            dataFrame.data = data
            if (byteBuffer.remaining() != dataFrame.padLength.toInt()) {
                throw HttpFrameException("Invalid frame padding length")
            }
            return dataFrame
        }
    }
}
