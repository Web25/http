package org.web25.http

import org.jetbrains.annotations.Contract
import org.web25.http.transport.Http11Transport
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by felix on 6/9/16.
 */
interface HttpTransport : Driver {

    fun write(httpRequest: HttpRequest, outputStream: OutputStream)
    fun write(httpResponse: HttpResponse, outputStream: OutputStream, entityStream: InputStream?)

    @Throws(IOException::class)
    fun readRequest(inputStream: InputStream): HttpRequest

    @Throws(IOException::class)
    fun readResponse(inputStream: InputStream, pipe: OutputStream?): HttpResponse

    companion object {

        val version11: ThreadLocal<HttpTransport> = object : ReadonlyThreadLocal<HttpTransport>() {
            @Contract(" -> !null")
            override fun initialValue(): HttpTransport {
                return Http11Transport()
            }

        }

        fun version11(): HttpTransport {
            return version11.get()
        }

        fun def(): HttpTransport {
            return version11()
        }
    }
}
