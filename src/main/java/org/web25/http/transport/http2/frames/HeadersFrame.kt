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
class HeadersFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
(settings: HttpSettings) : PaddedFrame(settings) {

    var isE: Boolean = false
    var streamDependency: Int = 0
    var weight: Short = 0
    var headerBlockFragment: ByteArray? = null

    init {
        type = Constants.Http20.FrameType.HEADERS
    }

    var isEndHeaders: Boolean
        get() = flags.toInt() and 0x4 == 0x4
        set(endHeaders) {
            flags = (flags.toInt() and 0x4.inv() or if (endHeaders) 0x4 else 0).toShort()
        }

    var isPriority: Boolean
        get() = flags.toInt() and 0x20 == 0x20
        set(priority) {
            flags = (flags.toInt() and 0x20.inv() or if (priority) 0x20 else 0).toShort()
        }

    override fun build() {
        var length = headerBlockFragment!!.size
        if (isPadded) {
            length++ //padding adds one byte
            length += padLength.toInt()
        }
        if (isPriority) {
            length += 5
        }
        val buffer = ByteBuffer.allocate(length)
        if (isPadded)
            buffer.put(HttpUtil.toByte(padLength), 1, 1)
        if (isPriority) {
            val dep = HttpUtil.toByte(streamDependency)
            if (isE) {
                dep[0] = (dep[0].toInt() or 128).toByte()
            }
            buffer.put(dep)
            buffer.put(HttpUtil.toByte(weight), 1, 1)
        }
        buffer.put(headerBlockFragment!!)
        payload = buffer.array()
    }

    companion object {

        fun from(frame: HttpFrame): HeadersFrame {
            val headersFrame = HeadersFrame(frame.settings)
            headersFrame.flags = frame.flags
            headersFrame.streamIdentifier = frame.streamIdentifier
            val byteBuffer = ByteBuffer.wrap(frame.payload)
            if (headersFrame.isPadded) {
                headersFrame.padLength = (byteBuffer.get().toInt() and 0xff).toShort()
            }
            if (headersFrame.isPriority) {
                val data = ByteArray(4)
                byteBuffer.get(data)
                if (data[0].toInt() and 0xff and 128 == 128) {
                    headersFrame.isE = true
                    data[0] = (data[0].toInt() and 128.inv()).toByte()
                }
                headersFrame.streamDependency = HttpUtil.toInt(data)
                headersFrame.weight = (byteBuffer.get().toInt() and 0xff).toShort()
            }
            val headerBlockFragment = ByteArray(byteBuffer.remaining() - headersFrame.padLength)
            byteBuffer.get(headerBlockFragment)
            headersFrame.headerBlockFragment = headerBlockFragment
            if (byteBuffer.remaining() != headersFrame.padLength.toInt()) {
                throw HttpFrameException("Invalid frame padding length")
            }
            return headersFrame
        }
    }
}
