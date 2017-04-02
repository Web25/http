package org.web25.http.drivers

import org.slf4j.LoggerFactory
import org.web25.http.HttpRequest
import org.web25.http.drivers.treehandler.HandlerRequestHandler
import org.web25.http.drivers.treehandler.MethodMatchers
import org.web25.http.drivers.treehandler.MiddlewareRequestHandler
import org.web25.http.drivers.treehandler.TreeHandler
import org.web25.http.path.HttpPath
import org.web25.http.server.*

/**
 * Created by Felix Resch on 29-Apr-16.
 */
class TreeHttpRouter : HttpRouter {

    val treeHandler: TreeHandler = TreeHandler()

    val httpPath: HttpPath = HttpPath("/")


    override fun use(handler: HttpMiddleware): HttpRouter {
        treeHandler.submit(httpPath, MiddlewareRequestHandler(handler))
        return this
    }

    override fun use(path: String, handler: HttpMiddleware): HttpRouter {
        val httpPath = HttpPath(path)
        treeHandler.submit(httpPath, MiddlewareRequestHandler(handler))
        return this
    }

    override fun fallback(handler: HttpHandler): HttpRouter {
        treeHandler.fallback(handler)
        return this
    }

    override fun use(path: String, httpHandler: HttpHandler): HttpRouter {
        val httpPath = HttpPath(path)
        if(httpHandler is HttpRouter) {
            if(httpHandler is TreeHttpRouter) {
                treeHandler.injectChild(httpPath, httpHandler.treeHandler.root)
            } else {
                throw TreeRouterException("Other types of routers aren't supported")
            }
        } else {
            treeHandler.submit(httpPath, HandlerRequestHandler(httpHandler, MethodMatchers.any))
        }
        return this
    }

    override fun use(method: String, path: String, httpHandler: HttpHandler): HttpRouter {
        val httpPath = HttpPath(path)
        if(httpHandler is HttpRouter) {
            LOGGER.warn("Router has been used for a call with a method. Ignoring!")
            if(httpHandler is TreeHttpRouter) {
                treeHandler.injectChild(httpPath, httpHandler.treeHandler.root)
            } else {
                throw TreeRouterException("Other types of routers aren't supported")
            }
        } else {
            treeHandler.submit(httpPath, HandlerRequestHandler(httpHandler, MethodMatchers[method]))
        }
        return this
    }

    override fun after(middleware: HttpMiddleware): HttpRouter {
        TODO()
        return this
    }

    override fun parentPath(path: String): HttpRouter {
        TODO("not implemented") //To change body of created functions fallback File | Settings | File Templates.
    }

    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse): Boolean {
        return treeHandler.handle(req, res)
    }

    override fun matches(httpRequest: HttpRequest): Boolean {
        TODO("not implemented") //To change body of created functions fallback File | Settings | File Templates.
    }

    override fun prependPath(path: String): HttpRoutable<HttpRouter> {
        TODO("not implemented") //To change body of created functions fallback File | Settings | File Templates.
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}

