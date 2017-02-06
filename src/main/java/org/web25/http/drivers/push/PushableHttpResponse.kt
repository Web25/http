package org.web25.http.drivers.push

import org.web25.http.HttpRequest
import org.web25.http.HttpResponse
import org.web25.http.drivers.DefaultHttpResponse
import java.util.*

/**
 * Created by felix on 9/19/16.
 */
class PushableHttpResponse(httpRequest: HttpRequest) : DefaultHttpResponse() {

    private val pushRequests: MutableList<PushRequest>

    init {
        this.request(httpRequest)
        this.pushRequests = ArrayList<PushRequest>()
    }

    override fun push(method: String, path: String): HttpResponse {
        val pushRequest = PushRequest(request())
        pushRequest.method(method)
        pushRequest.path(path)
        pushRequests.add(pushRequest)
        return this
    }

    fun getPushRequests(): List<PushRequest> {
        return pushRequests
    }
}
