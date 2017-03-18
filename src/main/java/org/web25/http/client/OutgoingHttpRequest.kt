package org.web25.http.client

import org.web25.http.*
import org.web25.http.entities.ByteArrayEntity
import org.web25.http.entities.StringEntity
import org.web25.http.entities.UrlFormEncodedEntity
import java.io.OutputStream

abstract class OutgoingHttpRequest(context : HttpContext): HttpRequest(context) {

    abstract fun method(method: String): OutgoingHttpRequest

    abstract fun header(name: String, value: String): OutgoingHttpRequest
    fun header(name: String, value: Number) = header(name, value.toString())

    fun entity(entity: HttpEntity): OutgoingHttpRequest {
        this.entity = entity
        try {
            header("Content-Length", entity.getLength())
        } catch (ignored: Throwable) {}
        return contentType(entity.contentType)
    }

    fun entity(entity: ByteArray): OutgoingHttpRequest = entity(ByteArrayEntity(entity))

    fun entity(entity: String): OutgoingHttpRequest = entity(StringEntity(entity))

    fun entity(entity: Map<String, Any>): OutgoingHttpRequest {
        val urlFormEncodedEntity = UrlFormEncodedEntity()
        urlFormEncodedEntity.putAll(entity.map { Pair(it.key, it.value.toString()) })
        return entity(urlFormEncodedEntity as HttpEntity)
    }

    abstract fun entity(entity: Any): OutgoingHttpRequest
    abstract fun execute(callback: ((HttpResponse) -> Unit)?): OutgoingHttpRequest
    abstract fun transport(transport: Transport): OutgoingHttpRequest
    abstract fun version(version: HttpVersion): OutgoingHttpRequest

    abstract fun use(httpTransport: HttpTransport): OutgoingHttpRequest
    abstract fun port(port: Int): OutgoingHttpRequest
    abstract fun host(host: String): OutgoingHttpRequest
    abstract fun pipe(outputStream: OutputStream): OutgoingHttpRequest

    fun execute(): OutgoingHttpRequest {
        return execute(null)
    }


    fun contentType(contentType: String): OutgoingHttpRequest {
        return header("Content-Type", contentType)
    }

    fun https(): OutgoingHttpRequest {
        return transport(Transport.HTTPS)
    }

    fun cookie(name: String, value: String): OutgoingHttpRequest {
        cookies[name] = value
        return this
    }

    override fun path(path: String): OutgoingHttpRequest {
        super.path(path)
        return this
    }

    abstract fun host(): String
    operator fun invoke(): HttpResponse {
        execute()
        return response()
    }
}