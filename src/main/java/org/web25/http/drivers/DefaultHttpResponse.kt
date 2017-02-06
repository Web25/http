package org.web25.http.drivers

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.web25.http.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


/**
 * Created by felix on 9/11/15.
 */
open class DefaultHttpResponse : HttpResponse() {

    lateinit var statusCode: StatusCode
    private val headers: MutableMap<String, HttpHeader> = mutableMapOf()
    private val cookies: MutableMap<String, HttpCookie> = mutableMapOf()
    var entity: ByteArray = byteArrayOf()
    private var httpTransport: HttpTransport? = null

    private var entityStream: InputStream? = null
    lateinit var request: HttpRequest

    override fun status(statusCode: StatusCode): HttpResponse {
        this.statusCode = statusCode
        return this
    }

    override fun entity(entity: String): HttpResponse {
        return entity(entity.toByteArray())
    }

    override fun entity(entity: ByteArray): HttpResponse {
        header("Content-Length", entity.size.toString())
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entity = entity
        return this
    }

    override fun entity(inputStream: InputStream): HttpResponse {
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entityStream = inputStream
        return this
    }

    override fun hasHeader(name: String): Boolean {
        return headers.containsKey(name)
    }

    override fun header(name: String, value: String): HttpResponse {
        this.headers.put(name, HttpHeader(name, value))
        return this
    }

    override fun cookie(name: String): HttpCookie? {
        return cookies[name]
    }

    override fun hasCookie(name: String): Boolean {
        return cookies.containsKey(name)
    }

    override fun cookie(name: String, value: String): HttpResponse {
        this.cookies.put(name, HttpCookie(name, value))
        return this
    }

    override fun cookies(): Collection<HttpCookie> {
        return cookies.values
    }

    override fun print(outputStream: OutputStream) {
        if (httpTransport == null) {
            httpTransport = HttpTransport.def()
        }
        httpTransport!!.write(this, outputStream, entityStream!!)
    }

    override fun statusLine(): String {
        return status().status().toString() + " " + status().statusMessage()
    }

    override fun headers(): Collection<HttpHeader> {
        return headers.values
    }

    override fun push(method: String, path: String): HttpResponse {
        return this
    }

    override fun status(): StatusCode {
        return statusCode
    }

    override fun responseString(): String {
        return String(responseBytes())
    }

    override fun responseBytes(): ByteArray {
        if (request().method().equals("HEAD", ignoreCase = true)) {
            return byteArrayOf()
        } else if (entity.isEmpty() && entityStream != null) {
            try {
                this.entity = IOUtils.toByteArray(entityStream!!)
            } catch (e: IOException) {
                log.warn("Could not read resource", e)
            }

        }
        return entity
    }

    override fun request(request: HttpRequest) {
        this.request = request
    }

    override fun request(): HttpRequest {
        return this.request
    }

    override fun header(name: String): HttpHeader? {
        return headers[name]
    }

    companion object {

        private val log = LoggerFactory.getLogger("HTTP")
    }

}
