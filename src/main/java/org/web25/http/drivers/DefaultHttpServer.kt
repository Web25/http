package org.web25.http.drivers

import org.slf4j.LoggerFactory
import org.web25.http.*
import org.web25.http.drivers.server.*

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


/**
 * Created by felix on 2/24/16.
 */
open class DefaultHttpServer(var port: Int, protected var ssl: Boolean) : HttpServer {

    var httpHandlerStack: HttpHandlerStack

    protected var serverThread: HttpServerThread? = null

    init {
        this.httpHandlerStack = HttpHandlerStack()
        use({ req: HttpRequest, res: HttpResponse -> res.header("Date", ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME)) } as HttpMiddleware)
    }

    override fun start(): HttpServer {
        use({ req: HttpRequest, res: HttpResponse ->
            res.status(StatusCode.NOT_FOUND)
            res.entity("Could not find resource at " + req.method().toUpperCase() + " " + req.path())
            true
        } as HttpHandler)
        if (serverThread == null)
            this.serverThread = HttpServerThread(httpHandlerStack)
        serverThread!!.port = port
        serverThread!!.start()
        LOGGER.info("Started HTTP Server on port {}", port)
        return this
    }

    override fun stop(): HttpServer {
        this.serverThread!!.interrupt()
        return this
    }

    override fun secure(): HttpsServer {
        if (this is HttpsServer) {
            return this
        }
        return DefaultHttpsServer(this)
    }

    final override fun use(handler: HttpMiddleware): HttpServer {
        val handle = HttpMiddlewareHandle()
        handle.httpMiddleware = handler
        httpHandlerStack.submit(handle)
        return this
    }

    override fun use(path: String, handler: HttpMiddleware): HttpServer {
        val handle = HttpMiddlewareHandle()
        handle.path = path
        handle.httpMiddleware = handler
        httpHandlerStack.submit(handle)
        return this
    }

    override fun use(handler: HttpHandler): HttpServer {
        if (handler is HttpRouter) {
            val handle = HttpRouterHandle()
            handler.parentPath("/")
            handle.setRouter(handler)
            httpHandlerStack.submit(handle)
        } else {
            val handle = HttpHandlerHandle()
            handle.handler = handler
            httpHandlerStack.submit(handle)
        }
        return this
    }

    override fun use(path: String, httpHandler: HttpHandler): HttpServer {
        if (httpHandler is HttpRoutable<*>) {
            val handle = HttpRouterHandle()
            (httpHandler as HttpRouter).parentPath(HttpRoutable.Companion.joinPaths("/", path))
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

    override fun use(method: String, path: String, httpHandler: HttpHandler): HttpServer {
        if (httpHandler is HttpRouter) {
            return this
        }
        val handle = HttpHandlerHandle()
        handle.handler = httpHandler
        handle.method = method
        handle.path = path
        httpHandlerStack.submit(handle)
        return this
    }

    override fun after(middleware: HttpMiddleware): HttpServer {
        httpHandlerStack.submitAfter(middleware)
        return this
    }

    override fun matches(httpRequest: HttpRequest): Boolean {
        return httpHandlerStack.matches(httpRequest)
    }

    override fun prependPath(path: String): HttpRoutable<HttpServer> {
        return this
    }

    override fun ready(): Boolean {
        return this.serverThread!!.ready()
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }

}