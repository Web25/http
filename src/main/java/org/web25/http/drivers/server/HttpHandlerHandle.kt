package org.web25.http.drivers.server

import org.web25.http.HttpRequest
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.HttpHandler
import org.web25.http.server.HttpRoutable.Companion.joinPaths
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse
import java.util.*

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
        val path = this.path
        if (path != null) {
            val requested = request.path()
            //first look if path or requested ends with / and the other one does not
            if (path.endsWith("/") && !requested.endsWith("/") || !path.endsWith("/") && requested.endsWith("/")) {
                return false
            }

            // When path is dynamic
            if (path.contains("{") && path.contains("}")) {
                val tokenizedPath = StringTokenizer(path, "/")
                val tokenizedRequ = StringTokenizer(requested, "/")

                if (tokenizedPath.countTokens() != tokenizedRequ.countTokens())
                    return false

                while (tokenizedPath.hasMoreElements()) {
                    val pathPart = tokenizedPath.nextToken()
                    val requPart = tokenizedRequ.nextToken()

                    if (pathPart.contains("{") && pathPart.contains("}")) {
                        //extract attribute name and value
                    } else {
                        if (pathPart != requPart)
                            return false
                    }
                }
                return true
            }

            // When path is static
            if (requested != path) {
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
