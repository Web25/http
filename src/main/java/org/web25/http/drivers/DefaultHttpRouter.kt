package org.web25.http.drivers

import org.slf4j.LoggerFactory
import org.web25.http.*
import org.web25.http.drivers.server.HttpHandlerHandle
import org.web25.http.drivers.server.HttpHandlerStack
import org.web25.http.drivers.server.HttpMiddlewareHandle
import org.web25.http.drivers.server.HttpRouterHandle

/**
 * Created by Felix Resch on 29-Apr-16.
 */
class DefaultHttpRouter : HttpRouter {

    private var parentPath: String? = null
    private val httpHandlerStack: HttpHandlerStack

    init {
        this.httpHandlerStack = HttpHandlerStack()
    }

    override fun parentPath(path: String): HttpRouter {
        this.parentPath = path
        httpHandlerStack.prependPath(path)
        return this
    }

    @Throws(HttpHandleException::class)
    override fun handle(request: HttpRequest, response: HttpResponse): Boolean {
        return httpHandlerStack.handle(request, response)
    }

    override fun use(handler: HttpMiddleware): HttpRouter {
        val handle = HttpMiddlewareHandle()
        handle.httpMiddleware = handler
        httpHandlerStack.submit(handle)
        return this
    }

    override fun use(path: String, handler: HttpMiddleware): HttpRouter {
        val handle = HttpMiddlewareHandle()
        handle.path = path
        handle.httpMiddleware = handler
        httpHandlerStack.submit(handle)
        return this
    }

    override fun use(handler: HttpHandler): HttpRouter {
        if (handler is HttpRouter) {
            val handle = HttpRouterHandle()
            handler.parentPath(parentPath!!)
            handle.setRouter(handler)
            httpHandlerStack.submit(handle)
        } else {
            val handle = HttpHandlerHandle()
            handle.handler = handler
            httpHandlerStack.submit(handle)
        }
        return this
    }

    override fun use(path: String, httpHandler: HttpHandler): HttpRouter {
        if (httpHandler is HttpRouter) {
            val handle = HttpRouterHandle()
            httpHandler.parentPath(path)
            if (this.parentPath != null) {
                httpHandler.prependPath(parentPath!!)
            }
            handle.setRouter(httpHandler)
            httpHandlerStack.submit(handle)
        } else {
            val handle = HttpHandlerHandle()
            handle.handler = httpHandler
            handle.path = path
            httpHandlerStack.submit(handle)
        }
        return this
    }

    override fun use(method: String, path: String, httpHandler: HttpHandler): HttpRouter {
        if (httpHandler is HttpRouter) {
            LOGGER.warn("Attempting to bind router with method {}. Ignoring", method.toUpperCase())
            return this
        }
        val handle = HttpHandlerHandle()
        handle.handler = httpHandler
        handle.method = method
        handle.path = path
        httpHandlerStack.submit(handle)
        return this
    }

    override fun after(middleware: HttpMiddleware): HttpRouter {
        httpHandlerStack.submitAfter(middleware)
        return this
    }

    override fun matches(httpRequest: HttpRequest): Boolean {
        return httpRequest.path().startsWith(this.parentPath!!) && httpHandlerStack.matches(httpRequest)
    }

    override fun prependPath(path: String): HttpRoutable<HttpRouter> {
        this.parentPath = HttpRoutable.Companion.joinPaths(path, this.parentPath)
        this.httpHandlerStack.prependPath(path)
        return this
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}
