package org.web25.http.drivers.treehandler

import org.web25.http.server.HttpHandler
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

class HandlerRequestHandler(val httpHandler: HttpHandler, methodMatcher: MethodMatcher): RequestHandler(methodMatcher) {

    override fun handle(req: IncomingHttpRequest, res: OutgoingHttpResponse): Boolean = httpHandler(req, res)

    override fun matches(req: IncomingHttpRequest): Boolean = methodMatcher(req.method)

}