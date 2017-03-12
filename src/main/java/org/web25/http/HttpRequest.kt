package org.web25.http

import org.web25.http.drivers.Driver

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpRequest(val context : HttpContext) {

    abstract fun using(driver: Driver): HttpRequest

    abstract fun prepareEntity(): HttpRequest

    abstract fun method(): String

    val cookies = CookieList()
    val headers = HeaderList()

    abstract val getParameters: MutableMap<String, Any>
    abstract val postParameters: MutableMap<String, Any>

    abstract fun entityBytes(): ByteArray
    abstract fun entityString(): String


    abstract fun response(): HttpResponse


    abstract fun transport(): Transport
    abstract fun requestLine(): String

    fun hasHeader(name: String): Boolean = name in headers
    fun hasHeaders(vararg names: String): Boolean = names.all { hasHeader(it) }

    abstract fun path(): String

}

