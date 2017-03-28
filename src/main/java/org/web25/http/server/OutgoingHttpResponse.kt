package org.web25.http.server

import org.web25.http.HttpContext
import org.web25.http.HttpCookie
import org.web25.http.HttpResponse
import org.web25.http.StatusCode
import org.web25.http.drivers.DefaultHttpResponse
import org.web25.http.entities.ByteArrayEntity
import org.web25.http.entities.StringEntity
import java.io.InputStream

abstract class OutgoingHttpResponse(context : HttpContext): DefaultHttpResponse(context) {

    abstract var finished: Boolean
    abstract fun push(method: String, path: String): OutgoingHttpResponse

    fun entity(entity: String): OutgoingHttpResponse {
        this.entity = StringEntity(entity)
        return this
    }

    fun entity(entity: ByteArray): OutgoingHttpResponse {
        this.entity = ByteArrayEntity(entity)
        return this
    }

    fun entity(entity: Any): OutgoingHttpResponse {
        this.entity = context.registry.serialize(entity)
        return this
    }

    abstract fun entityStream(): InputStream?

    abstract fun status(statusCode: StatusCode): OutgoingHttpResponse
    abstract fun header(name: String, value: String): OutgoingHttpResponse
    abstract fun cookie(name: String, value: String): OutgoingHttpResponse

    fun status(status: Int): HttpResponse = status(StatusCode.find(status))
    fun cookie(cookie : HttpCookie): OutgoingHttpResponse {
        cookies[cookie.name] = cookie
        return this
    }

}