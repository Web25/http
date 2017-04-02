package org.web25.http.drivers.treehandler

import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

abstract class RequestHandler(val methodMatcher: MethodMatcher) {

    abstract fun handle (req: IncomingHttpRequest, res: OutgoingHttpResponse): Boolean
    abstract fun matches (req: IncomingHttpRequest): Boolean
}