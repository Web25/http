package org.web25.http

import org.web25.http.drivers.Driver
import org.web25.http.exceptions.HeaderNotFoundException
import org.web25.http.path.HttpPath

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpRequest(val context : HttpContext) {

    abstract fun using(driver: Driver): HttpRequest

    abstract fun prepareEntity(): HttpRequest

    abstract fun method(): String

    val cookies = CookieList()

    abstract val headers: MutableMap<String, HttpHeader>
    abstract val query: MutableMap<String, Any>
    
    abstract fun entityBytes(): ByteArray
    abstract fun entityString(): String

    abstract fun response(): HttpResponse


    abstract fun transport(): Transport
    abstract fun requestLine(): String


    @Throws(HeaderNotFoundException::class)
    abstract fun header(name: String): HttpHeader
    fun hasHeader(name: String): Boolean = headers.containsKey(name.toLowerCase())

    fun hasHeaders(vararg names: String): Boolean = names.all { hasHeader(it) }

    lateinit var path: HttpPath
    private set

    open fun path(path: String): HttpRequest {
        this.path = HttpPath(path)
        return this
    }

}

