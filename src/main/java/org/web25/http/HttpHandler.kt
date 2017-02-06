package org.web25.http


/**
 * Created by Felix Resch on 25-Apr-16.
 */
@FunctionalInterface
interface HttpHandler {

    @Throws(HttpHandleException::class)
    fun handle(request: HttpRequest, response: HttpResponse): Boolean
}
