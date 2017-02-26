package org.web25.http

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpResponse(val context : HttpContext) {

    abstract fun status(): StatusCode
    abstract fun responseString(): String
    abstract fun responseBytes(): ByteArray

    abstract fun request(request: HttpRequest)
    abstract fun request(): HttpRequest

    val cookies = CookieList()
    val headers = HeaderList()

    fun hasCookie(name: String): Boolean = name in cookies
    fun hasHeader(name: String): Boolean = name in headers

    fun statusCode(): Int {
        return status().status()
    }

    abstract fun statusLine(): String

}