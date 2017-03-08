package org.web25.http.server

/**
 * Created by felix on 3/8/17.
 */
interface IncomingRequestHandler {

    fun handle(request: IncomingHttpRequest, response: OutgoingHttpResponse): Boolean
}