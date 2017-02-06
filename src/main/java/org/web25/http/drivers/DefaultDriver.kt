package org.web25.http.drivers

import org.web25.http.HttpDriver
import org.web25.http.HttpRequest
import org.web25.http.HttpServer

import java.net.URL

/**
 * Created by felix on 9/11/15.
 */
open class DefaultDriver : HttpDriver() {

    override fun url(url: URL): HttpRequest {
        val request = DefaultHttpRequest().path(url.path).port(url.port).host(url.host)
        if (url.protocol.toLowerCase() == "https") {
            request.https()
        }
        return request
    }

    override fun server(port: Int, ssl: Boolean): HttpServer {
        if (ssl) {
            return DefaultHttpsServer(port)
        } else {
            return DefaultHttpServer(port, ssl)
        }
    }
}
