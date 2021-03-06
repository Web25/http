package org.web25.http

import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.server.Configurator
import org.web25.http.server.HttpRouter
import org.web25.http.server.HttpServer
import java.net.URL

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpDriver(val context : HttpContext) {

    abstract fun openRequest(url: URL): OutgoingHttpRequest

    fun openRequest(url: String): OutgoingHttpRequest = openRequest(URL(url))

    abstract fun server(port: Int, ssl: Boolean = false): HttpServer
    abstract fun server(configurator: Configurator): HttpServer
    abstract fun router(): HttpRouter
}
