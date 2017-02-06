package org.web25.http

import java.net.URL

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpDriver {

    abstract fun url(url: URL): HttpRequest

    fun url(url: String): HttpRequest = url(URL(url))

    abstract fun server(port: Int, ssl: Boolean): HttpServer
}
