package org.web25.http.drivers

import org.web25.http.*


/**
 * Created by felix on 9/11/15.
 */
open class DefaultHttpResponse(context : HttpContext) : HttpResponse(context) {

    var statusCode: StatusCode = StatusCode.OK
    var entity: ByteArray = byteArrayOf()
    private var httpTransport: HttpTransport? = null
    lateinit var request: HttpRequest

    override fun statusLine(): String {
        return status().status().toString() + " " + status().statusMessage()
    }

    override fun status(): StatusCode {
        return statusCode
    }

    override fun responseString(): String {
        return String(responseBytes())
    }

    override fun responseBytes(): ByteArray {
        return entity
    }

    override fun request(request: HttpRequest) {
        this.request = request
    }

    override fun request(): HttpRequest {
        return this.request
    }

}
