package org.web25.http.auth2

import org.web25.http.HttpResponse
import org.web25.http.client.OutgoingHttpRequest

/**
 * Created by felix on 3/31/17.
 */
class BasicAuthentication(override val realm: String, override val host: String,
                          var username: String? = null, var password: String? = null) : Authentication {

    override fun authenticate(response: HttpResponse, request: OutgoingHttpRequest) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun matchesAuthentication(request: OutgoingHttpRequest): Boolean {
        if(!request.hasHeader("Host"))
            return false
        val host = request.headers["Host"]
        if(host != this.host)
            return false
        return true
    }
}