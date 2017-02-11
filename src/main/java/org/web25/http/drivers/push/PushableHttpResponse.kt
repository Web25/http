package org.web25.http.drivers.push

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.web25.http.*
import org.web25.http.server.OutgoingHttpResponse
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Created by felix on 9/19/16.
 */
class PushableHttpResponse(httpRequest: HttpRequest, context : HttpContext = httpRequest.context) : OutgoingHttpResponse(context) {

    private val log = LoggerFactory.getLogger("HTTP")

    override var finished: Boolean = false
    private var entityStream: InputStream? = null

    private val pushRequests: MutableList<PushRequest>

    init {
        this.request(httpRequest)
        this.pushRequests = ArrayList<PushRequest>()
    }

    override fun push(method: String, path: String): OutgoingHttpResponse {
        val pushRequest = PushRequest(request(), context)
        pushRequest.method(method)
        pushRequest.path(path)
        pushRequests.add(pushRequest)
        return this
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

    fun getPushRequests(): List<PushRequest> {
        return pushRequests
    }

    override fun entity(entity: String): OutgoingHttpResponse {
        return entity(entity.toByteArray())
    }

    override fun entity(entity: ByteArray): OutgoingHttpResponse {
        header("Content-Length", entity.size.toString())
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entity = entity
        return this
    }

    override fun entity(inputStream: InputStream): OutgoingHttpResponse {
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entityStream = inputStream
        return this
    }

    override fun entityStream(): InputStream? {
        return this.entityStream
    }

    override fun status(statusCode: StatusCode): OutgoingHttpResponse {
        this.statusCode = statusCode
        return this
    }

    override fun header(name: String, value: String): OutgoingHttpResponse {
        this.headers.put(name, HttpHeader(name, value))
        return this
    }


    override fun cookie(name: String, value: String): OutgoingHttpResponse {
        this.cookies.put(name, HttpCookie(name, value))
        return this
    }
}
