package org.web25.http.server

/**
 * Created by Felix Resch on 29-Apr-16.
 */
interface HttpRouter : HttpHandler, HttpRoutable<HttpRouter> {

    fun parentPath(path: String): HttpRouter

}
