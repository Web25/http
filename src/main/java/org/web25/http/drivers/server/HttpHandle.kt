package org.web25.http.drivers.server

import org.web25.http.HttpRequest
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

/**
 * Created by felix on 4/26/16.
 */
interface HttpHandle {

    fun matches(request: HttpRequest): Boolean

    @Throws(HttpHandleException::class)
    fun handle(request: IncomingHttpRequest, response: OutgoingHttpResponse): Boolean

    fun parentPath(path: String)

    fun prependPath(path: String)
}
