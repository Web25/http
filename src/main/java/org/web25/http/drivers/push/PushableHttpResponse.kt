package org.web25.http.drivers.push

import org.slf4j.LoggerFactory
import org.web25.http.HttpContext
import org.web25.http.HttpRequest
import org.web25.http.StatusCode
import org.web25.http.server.OutgoingHttpResponse
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

    override fun responseBytes(): ByteArray = entity?.getBytes() ?: byteArrayOf()

    fun getPushRequests(): List<PushRequest> {
        return pushRequests
    }

    override fun entityStream(): InputStream? {
        return this.entityStream
    }

    override fun status(statusCode: StatusCode): OutgoingHttpResponse {
        this.statusCode = statusCode
        return this
    }

    override fun header(name: String, value: String): OutgoingHttpResponse {
        this.headers[name] = value
        return this
    }


    override fun cookie(name: String, value: String): OutgoingHttpResponse {
        this.cookies[name] = value
        return this
    }
}
