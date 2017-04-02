package org.web25.http.server

import org.web25.http.exceptions.HttpHandleException

/**
 * Created by felix on 2/10/17.
 */
@FunctionalInterface
interface HttpExecutable<out T: Any> {

    @Throws(HttpHandleException::class)
    operator fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse): T
}