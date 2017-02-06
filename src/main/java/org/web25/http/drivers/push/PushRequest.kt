package org.web25.http.drivers.push

import org.web25.http.HttpRequest
import org.web25.http.drivers.IncomingHttpRequest

/**
 * Created by felix on 9/19/16.
 */
class PushRequest(httpRequest: HttpRequest) : IncomingHttpRequest() {

    init {
        if (httpRequest.hasHeader("User-Agent"))
            header("User-Agent", httpRequest.header("User-Agent").value)
        if (httpRequest.hasHeader(":authority"))
            header(":authority", httpRequest.header(":authority").value)
        if (httpRequest.hasHeader(":scheme"))
            header(":scheme", httpRequest.header(":scheme").value)
    }
}
