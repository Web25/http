package org.web25.http

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpResponse {

    abstract fun status(statusCode: StatusCode): HttpResponse

    fun status(status: Int): HttpResponse {
        return status(StatusCode.find(status))
    }

    abstract fun entity(entity: String): HttpResponse
    abstract fun entity(entity: ByteArray): HttpResponse
    abstract fun entity(inputStream: InputStream): HttpResponse

    abstract fun status(): StatusCode
    abstract fun responseString(): String
    abstract fun responseBytes(): ByteArray

    abstract fun request(request: HttpRequest)
    abstract fun request(): HttpRequest

    abstract fun header(name: String): HttpHeader?
    abstract fun hasHeader(name: String): Boolean
    abstract fun header(name: String, value: String): HttpResponse

    abstract fun cookie(name: String): HttpCookie?
    abstract fun hasCookie(name: String): Boolean
    abstract fun cookie(name: String, value: String): HttpResponse
    abstract fun cookies(): Collection<HttpCookie>

    fun statusCode(): Int {
        return status().status()
    }

    abstract fun print(outputStream: OutputStream)
    abstract fun statusLine(): String

    abstract fun headers(): Collection<HttpHeader>

    abstract fun push(method: String, path: String): HttpResponse
}
