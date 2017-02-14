package org.web25.http

import org.web25.http.Http.Methods.DELETE
import org.web25.http.Http.Methods.GET
import org.web25.http.Http.Methods.PATCH
import org.web25.http.Http.Methods.POST
import org.web25.http.Http.Methods.PUT
import org.web25.http.Http.Methods.UPDATE
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.DefaultHttpRouter
import org.web25.http.helper.DefaultHttpContext
import org.web25.http.server.Configurator
import org.web25.http.server.HttpRouter
import org.web25.http.server.HttpServer
import java.io.File
import java.net.URL
import java.util.*

/**
 * Created by felix on 9/10/15.
 */
class Http(val context : HttpContext = DefaultHttpContext(), private val driver: HttpDriver = HttpDrivers.defaultDriver(context)) {

    object Methods {
        val GET = "GET"
        val POST = "POST"
        val PUT = "PUT"
        val DELETE = "DELETE"
        val PATCH = "PATCH"
        val UPDATE = "UPDATE"
    }

    fun url(url: URL): OutgoingHttpRequest {
        return driver.openRequest(url)
    }

    fun url(url: String): OutgoingHttpRequest {
        return driver.openRequest(url)
    }

    fun get(url: URL): OutgoingHttpRequest {
        return url(url).method(GET)
    }

    fun post(url: URL): OutgoingHttpRequest {
        return url(url).method(POST)
    }

    fun put(url: URL): OutgoingHttpRequest {
        return url(url).method(PUT)
    }

    fun update(url: URL): OutgoingHttpRequest {
        return url(url).method(UPDATE)
    }

    fun delete(url: URL): OutgoingHttpRequest {
        return url(url).method(DELETE)
    }

    fun patch(url: URL): OutgoingHttpRequest {
        return url(url).method(PATCH)
    }

    operator fun get(url: String): OutgoingHttpRequest {
        return url(url).method(GET)
    }

    fun post(url: String): OutgoingHttpRequest {
        return url(url).method(POST)
    }

    fun put(url: String): OutgoingHttpRequest {
        return url(url).method(PUT)
    }

    fun update(url: String): OutgoingHttpRequest {
        return url(url).method(UPDATE)
    }

    fun delete(url: String): OutgoingHttpRequest {
        return url(url).method(DELETE)
    }

    fun patch(url: String): OutgoingHttpRequest {
        return url(url).method(PATCH)
    }

    fun server(port: Int, ssl: Boolean = false): HttpServer {
        return driver.server(port, ssl)
    }

    fun server(file: File): HttpServer {
        var prop = Properties()
        prop.load(file.inputStream())
        return driver.server(prop.get("port").toString().toInt(), prop.get("ssl").toString().toBoolean())
    }

    fun server(configurator: Configurator): HttpServer {
        return driver.server(configurator)
    }

    fun router(): HttpRouter {
        return DefaultHttpRouter()
    }
}
