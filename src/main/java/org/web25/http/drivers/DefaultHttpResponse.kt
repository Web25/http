package org.web25.http.drivers

import org.web25.http.*
import org.web25.http.exceptions.CookieNotFoundException
import org.web25.http.exceptions.HeaderNotFoundException
import java.util.*


/**
 * Created by felix on 9/11/15.
 */
open class DefaultHttpResponse(context : HttpContext) : HttpResponse(context) {

    override val headers: MutableMap<String, HttpHeader> = TreeMap()
    override val cookies: MutableMap<String, HttpCookie> = TreeMap()

    var statusCode: StatusCode = StatusCode.OK
    var entity: ByteArray = byteArrayOf()
    private var httpTransport: HttpTransport? = null
    lateinit var request: HttpRequest




    override fun cookie(name: String): HttpCookie {
        if(hasCookie(name)) {
            return cookies[name]!!
        } else {
            throw CookieNotFoundException(name)
        }
    }

    override fun statusLine(): String {
        return status().status().toString() + " " + status().statusMessage()
    }

    override fun status(): StatusCode {
        return statusCode
    }

    override fun responseString(): String {
        return String(responseBytes())
    }

    override fun responseBytes(): ByteArray {
        return entity
    }

    override fun request(request: HttpRequest) {
        this.request = request
    }

    override fun request(): HttpRequest {
        return this.request
    }

    override fun header(name: String): HttpHeader {
        if(hasHeader(name)) {
            return headers[name]!!
        } else {
            throw HeaderNotFoundException(name)
        }
    }

}
