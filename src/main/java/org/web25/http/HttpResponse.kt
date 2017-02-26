package org.web25.http

import org.web25.http.exceptions.HeaderNotFoundException

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpResponse(val context : HttpContext) {

    abstract fun status(): StatusCode
    abstract fun responseString(): String
    abstract fun responseBytes(): ByteArray

    abstract fun request(request: HttpRequest)
    abstract fun request(): HttpRequest

    @Throws(HeaderNotFoundException::class)
    abstract fun header(name: String): HttpHeader

    fun hasHeader(name: String): Boolean = headers.containsKey(name)

    fun statusCode(): Int {
        return status().status()
    }

    abstract fun statusLine(): String

    abstract val headers: MutableMap<String, HttpHeader>
}