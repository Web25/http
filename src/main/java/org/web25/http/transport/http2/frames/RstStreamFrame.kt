package org.web25.http.transport.http2.frames

import org.web25.http.Constants
import org.web25.http.transport.http2.HttpFrame
import org.web25.http.transport.http2.HttpFrameException
import org.web25.http.transport.http2.HttpSettings
import org.web25.http.transport.http2.HttpUtil

/**
 * Created by felix on 9/18/16.
 */
class RstStreamFrame @JvmOverloads constructor(settings: HttpSettings, var errorCode: Int = Constants.Http20.ErrorCodes.NO_ERROR) : HttpFrame(settings) {

    init {
        type = Constants.Http20.FrameType.RST_STREAM
    }

    override fun build() {
        payload = HttpUtil.toByte(errorCode)
    }

    companion object {

        fun from(frame: HttpFrame): RstStreamFrame {
            if (frame.length != 4) {
                throw HttpFrameException("Invalid length for RST_STREAM frame")
            }
            if (frame.type != Constants.Http20.FrameType.RST_STREAM) {
                throw HttpFrameException("Invalid type for RST_STREAM frame")
            }
            val rstStreamFrame = RstStreamFrame(frame.settings)
            rstStreamFrame.streamIdentifier = frame.streamIdentifier
            rstStreamFrame.flags = frame.flags
            rstStreamFrame.errorCode = HttpUtil.toInt(frame.payload)
            return rstStreamFrame
        }
    }
}
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
