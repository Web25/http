package org.web25.http

import org.jetbrains.annotations.Contract
import org.web25.http.drivers.DefaultDriver
import org.web25.http.drivers.DefaultHttpRouter
import java.net.URL

/**
 * Created by felix on 9/10/15.
 */
object Http {

    val GET = "GET"
    val POST = "POST"
    val PUT = "PUT"
    val DELETE = "DELETE"
    val PATCH = "PATCH"
    val UPDATE = "UPDATE"

    private var driver: HttpDriver = DefaultDriver()

    fun installDriver(driver: HttpDriver) {
        Http.driver = driver
    }

    fun url(url: URL): HttpRequest {
        return driver.url(url)
    }

    fun url(url: String): HttpRequest {
        return driver.url(url)
    }

    operator fun get(url: URL): HttpRequest {
        return url(url).method("GET")
    }

    fun post(url: URL): HttpRequest {
        return url(url).method("POST")
    }

    fun put(url: URL): HttpRequest {
        return url(url).method("PUT")
    }

    fun update(url: URL): HttpRequest {
        return url(url).method("UPDATE")
    }

    fun delete(url: URL): HttpRequest {
        return url(url).method("DELETE")
    }

    fun patch(url: URL): HttpRequest {
        return url(url).method("PATCH")
    }

    operator fun get(url: String): HttpRequest {
        return url(url).method("GET")
    }

    fun post(url: String): HttpRequest {
        return url(url).method("POST")
    }

    fun put(url: String): HttpRequest {
        return url(url).method("PUT")
    }

    fun update(url: String): HttpRequest {
        return url(url).method("UPDATE")
    }

    fun delete(url: String): HttpRequest {
        return url(url).method("DELETE")
    }

    fun patch(url: String): HttpRequest {
        return url(url).method("PATCH")
    }

    @JvmOverloads fun server(port: Int, ssl: Boolean = false): HttpServer {
        return driver.server(port, ssl)
    }

    @Contract(" -> !null")
    fun router(): HttpRouter {
        return DefaultHttpRouter()
    }
}
