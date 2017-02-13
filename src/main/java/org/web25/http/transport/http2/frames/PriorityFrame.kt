package org.web25.http.transport.http2.frames

import org.web25.http.Constants
import org.web25.http.transport.http2.HttpFrame
import org.web25.http.transport.http2.HttpSettings
import org.web25.http.transport.http2.HttpUtil

import java.nio.ByteBuffer

/**
 * Created by felix on 9/18/16.
 */
class PriorityFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
(settings: HttpSettings) : HttpFrame(settings) {

    var isE: Boolean = false
    var streamDependency: Int = 0
    var weight: Short = 0

    init {
        type = Constants.Http20.FrameType.PRIORITY
    }

    override fun build() {
        val buffer = ByteBuffer.allocate(5)
        val dep = HttpUtil.toByte(streamDependency)
        if (isE) {
            dep[0] = (dep[0].toInt() or 128).toByte()
        }
        buffer.put(dep)
        buffer.put(HttpUtil.toByte(weight), 1, 1)
        payload = buffer.array()
    }

    companion object {

        fun from(frame: HttpFrame): PriorityFrame {
            val priorityFrame = PriorityFrame(frame.settings)
            priorityFrame.flags = frame.flags
            priorityFrame.streamIdentifier = frame.streamIdentifier
            val byteBuffer = ByteBuffer.wrap(frame.payload)
            val data = ByteArray(4)
            byteBuffer.get(data)
            if (data[0].toInt() and 0xff and 128 == 128) {
                priorityFrame.isE = true
                data[0] = (data[0].toInt() and 128.inv()).toByte()
            }
            priorityFrame.streamDependency = HttpUtil.toInt(data)
            priorityFrame.weight = (byteBuffer.get().toInt() and 0xff).toShort()
            return priorityFrame
        }
    }
}
