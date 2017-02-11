package org.web25.http.drivers.server

import org.web25.http.HttpRequest
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.HttpHandler
import org.web25.http.server.HttpRoutable.Companion.joinPaths
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

/**
 * Created by felix on 4/25/16.
 */
class HttpHandlerHandle : HttpHandle {

    var method: String? = null
    var path: String? = null
    var handler: HttpHandler? = null

    override fun matches(request: HttpRequest): Boolean {
        if (method != null) {
            if (!request.method().equals(method!!, ignoreCase = true) && !(request.method().equals("HEAD", ignoreCase = true) && method!!.equals("GET", ignoreCase = true))) {
                return false
            }
        }
        if (path != null) {
            if (request.path() != path) {
                return false
            }
        }
        return true
    }

    @Throws(HttpHandleException::class)
    override fun handle(request: IncomingHttpRequest, response: OutgoingHttpResponse): Boolean {
        return handler!!(request, response)
    }

    override fun parentPath(path: String) {
        if (this.path != null)
            this.path = joinPaths(path, this.path)
    }

    override fun prependPath(path: String) {
        this.path = joinPaths(path, this.path)
    }
}
