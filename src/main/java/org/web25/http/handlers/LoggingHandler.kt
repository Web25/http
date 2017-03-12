package org.web25.http.handlers

import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.HttpMiddleware
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

/**
 * Created by felix on 6/6/16.
 */
class LoggingHandler : HttpMiddleware {

    @Throws(HttpHandleException::class)
    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse) {
        LOGGER.info("{} {} - {} {} {} byte(s)", req.method().toUpperCase(), req.path(),
                res.statusCode(), res.status().statusMessage(),
                if (res.hasHeader("Content-Length")) res.headers["Content-Length"].toInt() else "---")
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")

        @Contract(" -> !null")
        fun log(): HttpMiddleware {
            return LoggingHandler()
        }
    }
}
