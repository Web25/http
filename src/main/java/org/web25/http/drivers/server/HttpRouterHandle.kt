package org.web25.http.drivers.server

import org.web25.http.HttpRequest
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.HttpRouter
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

/**
 * Created by Felix Resch on 29-Apr-16.
 */
class HttpRouterHandle : HttpHandle {

    lateinit var httpRouter: HttpRouter

    override fun matches(request: HttpRequest): Boolean {
        return httpRouter.matches(request)
    }

    @Throws(HttpHandleException::class)
    override fun handle(request: IncomingHttpRequest, response: OutgoingHttpResponse): Boolean {
        return httpRouter(request, response)
    }

    override fun parentPath(path: String) {
        httpRouter.parentPath(path)
    }

    override fun prependPath(path: String) {
        this.httpRouter.prependPath(path)
    }

    fun setRouter(httpRouter: HttpRouter) {
        this.httpRouter = httpRouter
    }

    fun router(): HttpRouter {
        return this.httpRouter
    }
}
