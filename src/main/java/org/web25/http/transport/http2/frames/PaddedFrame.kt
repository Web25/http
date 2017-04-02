package org.web25.http.transport.http2.frames

import org.web25.http.transport.http2.HttpFrameException
import org.web25.http.transport.http2.HttpSettings

/**
 * Created by felix on 9/18/16.
 */
open class PaddedFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
(settings: HttpSettings) : EndStreamFrame(settings) {

    var padLength: Short = 0
        set(padLength) {
            if (padLength > 255) {
                throw HttpFrameException("Invalid value for padding")
            }
            field = padLength
        }

    var isPadded: Boolean
        get() = flags.toInt() and 0x8 == 0x8
        set(padded) {
            flags = (flags.toInt() and 0x8.inv() or if (padded) 0x8 else 0).toShort()
        }
}
