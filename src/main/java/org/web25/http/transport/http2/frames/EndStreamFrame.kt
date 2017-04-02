package org.web25.http.transport.http2.frames

import org.web25.http.transport.http2.HttpFrame
import org.web25.http.transport.http2.HttpSettings

/**
 * Created by felix on 9/18/16.
 */
open class EndStreamFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settings for the current HTTP/2,0 connection
 */
(settings: HttpSettings) : HttpFrame(settings) {

    var isEndStream: Boolean
        get() = flags.toInt() and 1 == 1
        set(endStream) {
            flags = (flags.toInt() and 254 or if (endStream) 1 else 0).toShort()
        }
}
