package org.web25.http.server

import org.web25.http.HttpContext
import org.web25.http.HttpResponse
import org.web25.http.StatusCode
import org.web25.http.drivers.DefaultHttpResponse
import java.io.InputStream

abstract class OutgoingHttpResponse(context : HttpContext): DefaultHttpResponse(context) {

    abstract var finished: Boolean
    abstract fun push(method: String, path: String): OutgoingHttpResponse

    abstract fun entity(entity: String): OutgoingHttpResponse
    abstract fun entity(entity: ByteArray): OutgoingHttpResponse
    abstract fun entity(inputStream: InputStream): OutgoingHttpResponse

    abstract fun entityStream(): InputStream?

    abstract fun status(statusCode: StatusCode): OutgoingHttpResponse
    abstract fun header(name: String, value: String): OutgoingHttpResponse
    abstract fun cookie(name: String, value: String): OutgoingHttpResponse

    fun status(status: Int): HttpResponse = status(StatusCode.find(status))

}