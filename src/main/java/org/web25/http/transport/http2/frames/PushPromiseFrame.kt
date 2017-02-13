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
class PushPromiseFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
(settings: HttpSettings) : PaddedFrame(settings) {

    var promisedStreamId: Int = 0
    var headerBlockFragment: ByteArray? = null

    init {
        type = Constants.Http20.FrameType.PUSH_PROMISE
    }

    var isEndHeaders: Boolean
        get() = flags.toInt() and 0x4 == 0x4
        set(endHeaders) {
            flags = (flags.toInt() and 0x4.inv() or if (endHeaders) 0x4 else 0).toShort()
        }

    override fun build() {
        var length = headerBlockFragment!!.size + 4    //stream identifier length
        if (isPadded) {
            length++ //padding adds one byte
            length += padLength.toInt()
        }
        val buffer = ByteBuffer.allocate(length)
        if (isPadded)
            buffer.put(HttpUtil.toByte(padLength), 1, 1)
        buffer.putInt(promisedStreamId)
        buffer.put(headerBlockFragment!!)
        payload = buffer.array()
    }

    companion object {

        fun from(frame: HttpFrame): PushPromiseFrame {
            val pushPromiseFrame = PushPromiseFrame(frame.settings)
            pushPromiseFrame.flags = frame.flags
            pushPromiseFrame.streamIdentifier = frame.streamIdentifier
            val byteBuffer = ByteBuffer.wrap(frame.payload)
            if (pushPromiseFrame.isPadded) {
                pushPromiseFrame.padLength = (byteBuffer.get().toInt() and 0xff).toShort()
            }
            pushPromiseFrame.promisedStreamId = byteBuffer.int
            val headerBlockFragment = ByteArray(byteBuffer.remaining() - pushPromiseFrame.padLength)
            byteBuffer.get(headerBlockFragment)
            pushPromiseFrame.headerBlockFragment = headerBlockFragment
            if (byteBuffer.remaining() != pushPromiseFrame.padLength.toInt()) {
                throw HttpFrameException("Invalid frame padding length")
            }
            return pushPromiseFrame
        }
    }
}
