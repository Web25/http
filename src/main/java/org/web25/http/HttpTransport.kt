package org.web25.http

import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.Driver
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse
import org.web25.http.transport.Http11Transport
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by felix on 6/9/16.
 */
interface HttpTransport : Driver {

    fun write(httpRequest: OutgoingHttpRequest, outputStream: OutputStream)
    fun write(httpResponse: OutgoingHttpResponse, outputStream: OutputStream, entityStream: InputStream? = null)

    @Throws(IOException::class)
    fun readRequest(inputStream: InputStream): IncomingHttpRequest

    @Throws(IOException::class)
    fun readResponse(inputStream: InputStream, pipe: OutputStream? = null, request: OutgoingHttpRequest): HttpResponse

    companion object {

        fun version11(context : HttpContext): HttpTransport {
            return Http11Transport(context)
        }

        fun def(context : HttpContext): HttpTransport {
            return version11(context)
        }
    }
}
