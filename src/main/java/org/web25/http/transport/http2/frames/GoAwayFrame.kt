package org.web25.http.transport.http2.frames

import org.web25.http.Constants
import org.web25.http.transport.http2.HttpFrame
import org.web25.http.transport.http2.HttpSettings
import org.web25.http.transport.http2.HttpUtil

import java.nio.ByteBuffer

/**
 * Created by felix on 9/12/16.
 */
class GoAwayFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
(settings: HttpSettings) : HttpFrame(settings) {

    var errorCode: Int = 0
    var lastStreamId: Int = 0
    var debugData: ByteArray? = null

    init {
        type = Constants.Http20.FrameType.GOAWAY
    }

    override fun build() {
        super.build()
        val buffer = ByteBuffer.allocate(2 * Integer.BYTES + if (debugData != null) debugData!!.size else 0)
        buffer.put(HttpUtil.toByte(lastStreamId))
        buffer.put(HttpUtil.toByte(errorCode))
        if (debugData != null) {
            buffer.put(debugData!!)
        }
        payload = buffer.array()
    }
}
