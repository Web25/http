package org.web25.http.drivers.server

import org.web25.http.HttpHandleException
import org.web25.http.HttpRequest
import org.web25.http.HttpResponse

/**
 * Created by felix on 4/26/16.
 */
interface HttpHandle {

    fun matches(request: HttpRequest): Boolean

    @Throws(HttpHandleException::class)
    fun handle(request: HttpRequest, response: HttpResponse): Boolean

    fun parentPath(path: String)

    fun prependPath(path: String)
}
