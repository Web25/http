package org.web25.http.drivers

import org.slf4j.LoggerFactory
import org.web25.http.HttpContext
import org.web25.http.HttpRequest
import org.web25.http.StatusCode
import org.web25.http.drivers.server.TreeHttpServerThread
import org.web25.http.drivers.treehandler.HandlerRequestHandler
import org.web25.http.drivers.treehandler.MethodMatchers
import org.web25.http.drivers.treehandler.MiddlewareRequestHandler
import org.web25.http.drivers.treehandler.TreeHandler
import org.web25.http.path.HttpPath
import org.web25.http.server.*
import org.web25.http.util.handler
import org.web25.http.util.middleware
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


/**
 * Created by felix on 2/24/16.
 */
open class TreeHttpServer(var port: Int, protected var ssl: Boolean, val context: HttpContext) : HttpServer {

    var treeHandler: TreeHandler = TreeHandler()

    protected var serverThread: TreeHttpServerThread? = null
    val httpPath = HttpPath("/")

    init {
        use(middleware { _, res -> res.header("Date", ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME)) })
    }

    constructor(configurator: Configurator, context: HttpContext) : this(configurator.getInt("port"), configurator.getBoolean("ssl"), context)

    override fun start(): HttpServer {
        fallback(handler { req, res ->
            res.status(StatusCode.NOT_FOUND)
            res.entity("Could not find resource at " + req.method().toUpperCase() + " " + req.path())
            true
        })
        if (serverThread == null)
            this.serverThread = TreeHttpServerThread(treeHandler, context)
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
        return TreeHttpsServer(this, context)
    }

    override fun use(handler: HttpMiddleware): HttpServer {
        treeHandler.submit(httpPath, MiddlewareRequestHandler(handler))
        return this
    }

    override fun use(path: String, handler: HttpMiddleware): HttpServer {
        val httpPath = HttpPath(path)
        treeHandler.submit(httpPath, MiddlewareRequestHandler(handler))
        return this
    }

    override fun fallback(handler: HttpHandler): HttpServer {
        treeHandler.fallback(handler)
        return this
    }

    override fun use(path: String, httpHandler: HttpHandler): HttpServer {
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

    override fun use(method: String, path: String, httpHandler: HttpHandler): HttpServer {
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

    override fun after(middleware: HttpMiddleware): HttpServer {
        treeHandler.submitAfter(middleware)
        return this
    }

    override fun matches(httpRequest: HttpRequest): Boolean {
        TODO()
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
