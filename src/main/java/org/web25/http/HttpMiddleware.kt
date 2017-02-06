package org.web25.http

/**
 * Created by felix on 4/26/16.
 */
@FunctionalInterface
interface HttpMiddleware {

    @Throws(HttpHandleException::class)
    fun handle(request: HttpRequest, response: HttpResponse)
}
