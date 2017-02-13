package org.web25.http.util

import org.web25.http.server.HttpHandler
import org.web25.http.server.HttpMiddleware
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

/**
 * Created by felix on 2/10/17.
 */

fun middleware (action: (IncomingHttpRequest, OutgoingHttpResponse) -> Unit): HttpMiddleware = object : HttpMiddleware {
    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse) = action(req, res)
}

fun handler (action : (IncomingHttpRequest, OutgoingHttpResponse) -> Boolean) = object : HttpHandler {
    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse): Boolean = action(req, res)

}
