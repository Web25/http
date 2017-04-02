package org.web25.http.drivers

import org.web25.http.HttpContext
import org.web25.http.HttpDriver
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.client.DefaultHttpRequest
import org.web25.http.server.Configurator
import org.web25.http.server.HttpRouter
import org.web25.http.server.HttpServer

import java.net.URL

/**
 * Created by felix on 9/11/15.
 */
open class DefaultDriver(context: HttpContext) : HttpDriver(context) {

    override fun openRequest(url: URL): OutgoingHttpRequest {
        val request = DefaultHttpRequest(context).port(normalizePort(url)).host(url.host).path(url.file)
        if (url.protocol.toLowerCase() == "https") {
            request.https()
        }
        return request
    }

    override fun server(port: Int, ssl: Boolean): HttpServer {
        if (ssl) {
            return TreeHttpsServer(port, context)
        } else {
            return TreeHttpServer(port, ssl, context)
        }
    }

    override fun server(configurator: Configurator): HttpServer {
        if(configurator.getBoolean("ssl")) {
            return TreeHttpsServer(configurator, context)
        } else {
            return TreeHttpServer(configurator, context);
        }
    }

    override fun router(): HttpRouter {
        return TreeHttpRouter()
    }

    fun normalizePort(url: URL): Int = if(url.port <= 0) url.defaultPort else url.port
}
