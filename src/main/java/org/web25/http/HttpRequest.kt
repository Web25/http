package org.web25.http

import org.web25.http.drivers.Driver
import org.web25.http.exceptions.CookieNotFoundException
import org.web25.http.exceptions.HeaderNotFoundException
import java.io.OutputStream

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpRequest(val context : HttpContext) {

    abstract fun using(driver: Driver): HttpRequest

    abstract fun pipe(outputStream: OutputStream): HttpRequest

    abstract fun prepareEntity(): HttpRequest

    abstract fun method(): String

    abstract val cookies: MutableMap<String, HttpCookie>
    abstract val headers: MutableMap<String, HttpHeader>

    abstract fun entityBytes(): ByteArray
    abstract fun entityString(): String


    abstract fun response(): HttpResponse


    abstract fun transport(): Transport
    abstract fun requestLine(): String


    @Throws(HeaderNotFoundException::class)
    abstract fun header(name: String): HttpHeader
    fun hasHeader(name: String): Boolean = headers.containsKey(name)

    fun hasHeaders(vararg names: String): Boolean = names.all { hasHeader(it) }

    abstract fun path(): String

    @Throws(CookieNotFoundException::class)
    abstract fun cookie(name: String): HttpCookie
    fun hasCookie(name: String): Boolean = headers.containsKey(name)
}

