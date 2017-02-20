package org.web25.http.client

import org.web25.http.*
import java.io.OutputStream

abstract class OutgoingHttpRequest(context : HttpContext): HttpRequest(context) {

    abstract fun method(method: String): OutgoingHttpRequest

    abstract fun cookie(name: String, value: String): OutgoingHttpRequest
    abstract fun header(name: String, value: String): OutgoingHttpRequest
    abstract fun entity(entity: ByteArray): OutgoingHttpRequest
    abstract fun entity(entity: String): OutgoingHttpRequest
    abstract fun entity(entity: Any): OutgoingHttpRequest
    abstract fun execute(callback: ((HttpResponse) -> Unit)?): OutgoingHttpRequest
    abstract fun transport(transport: Transport): OutgoingHttpRequest
    abstract fun version(version: HttpVersion): OutgoingHttpRequest
    abstract fun data(key: String, value: String): OutgoingHttpRequest

    abstract fun use(httpTransport: HttpTransport): OutgoingHttpRequest
    abstract fun port(port: Int): OutgoingHttpRequest
    abstract fun host(host: String): OutgoingHttpRequest
    abstract fun path(path: String): OutgoingHttpRequest
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

    abstract fun host(): String
}