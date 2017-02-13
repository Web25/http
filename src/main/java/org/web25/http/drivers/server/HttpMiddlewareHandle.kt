package org.web25.http.drivers.server

import org.web25.http.HttpRequest
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.HttpMiddleware
import org.web25.http.server.HttpRoutable.Companion.joinPaths
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse


/**
 * Created by felix on 4/26/16.
 */
class HttpMiddlewareHandle : HttpHandle {

    var path: String? = null
    var httpMiddleware: HttpMiddleware? = null

    override fun matches(request: HttpRequest): Boolean {
        if (path != null) {
            if (request.path() != path) {
                return false
            }
        }
        return true
    }

    @Throws(HttpHandleException::class)
    override fun handle(request: IncomingHttpRequest, response: OutgoingHttpResponse): Boolean {
        httpMiddleware!!(request, response)
        return false
    }

    override fun parentPath(path: String) {
        if (this.path != null)
            this.path = joinPaths(path, this.path)
    }

    override fun prependPath(path: String) {
        this.path = joinPaths(path, this.path)
    }
}
