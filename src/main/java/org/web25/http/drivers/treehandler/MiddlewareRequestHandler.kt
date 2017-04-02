package org.web25.http.drivers.treehandler

import org.web25.http.server.HttpMiddleware
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

class MiddlewareRequestHandler(val httpMiddleware: HttpMiddleware, methodMatcher: MethodMatcher = MethodMatchers.any): RequestHandler(methodMatcher) {

    override fun handle(req: IncomingHttpRequest, res: OutgoingHttpResponse): Boolean {
        httpMiddleware.invoke(req, res)
        return false
    }

    override fun matches(req: IncomingHttpRequest): Boolean = true

}