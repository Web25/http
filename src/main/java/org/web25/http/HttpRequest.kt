package org.web25.http

import org.web25.http.drivers.Driver
import org.web25.http.entities.AbstractStringableEntity
import org.web25.http.path.HttpPath
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpRequest(val context : HttpContext) {

    abstract fun using(driver: Driver): HttpRequest

    abstract fun method(): String

    val cookies = CookieList()
    val headers = HeaderList()

    val query: MutableMap<String, Any> by object : ReadOnlyProperty<HttpRequest, MutableMap<String, Any>>  {

        override fun getValue(thisRef: HttpRequest, property: KProperty<*>): MutableMap<String, Any> = path.query

    }

    val hasEntity: Boolean
    get() = entity != null

    fun entityBytes() = entity?.getBytes() ?: byteArrayOf()
    fun entityString(): String {
        val entity = this.entity
        if(entity != null) {
            if(entity is AbstractStringableEntity) {
                return entity.toString()
            } else {
                return String(entityBytes())
            }
        } else {
            throw HttpEntityException("Entity not set!")
        }
    }

    abstract fun response(): HttpResponse


    abstract fun transport(): Transport
    abstract fun requestLine(): String

    fun hasHeader(name: String): Boolean = name in headers
    fun hasHeaders(vararg names: String): Boolean = names.all { hasHeader(it) }

    lateinit var path: HttpPath
    private set

    open fun path(path: String): HttpRequest {
        this.path = HttpPath(path)
        return this
    }

    var entity: HttpEntity? = null

}

