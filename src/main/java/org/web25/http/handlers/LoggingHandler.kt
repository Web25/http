package org.web25.http.handlers

import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import org.web25.http.HttpHandleException
import org.web25.http.HttpMiddleware
import org.web25.http.HttpRequest
import org.web25.http.HttpResponse

/**
 * Created by felix on 6/6/16.
 */
class LoggingHandler : HttpMiddleware {

    @Throws(HttpHandleException::class)
    override fun handle(request: HttpRequest, response: HttpResponse) {
        LOGGER.info("{} {} - {} {} {} byte(s)", request.method().toUpperCase(), request.path(),
                response.statusCode(), response.status().statusMessage(),
                if (response.hasHeader("Content-Length")) response.header("Content-Length")!!.asInt() else "---")
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")

        @Contract(" -> !null")
        fun log(): HttpMiddleware {
            return LoggingHandler()
        }
    }
}
