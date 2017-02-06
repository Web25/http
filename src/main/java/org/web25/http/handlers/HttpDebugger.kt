package org.web25.http.handlers

import org.slf4j.LoggerFactory
import org.web25.http.HttpHandleException
import org.web25.http.HttpMiddleware
import org.web25.http.HttpRequest
import org.web25.http.HttpResponse
import java.io.PrintStream

/**
 * Created by felix on 6/6/16.
 */
class HttpDebugger(private val printStream: PrintStream) : HttpMiddleware {

    init {
        LOGGER.warn("Attention: HTTP Debugging has been activated! This might lead to excessive logging of HTTP Traffic!")
    }

    @Throws(HttpHandleException::class)
    override fun handle(request: HttpRequest, response: HttpResponse) {
        request.print(printStream)
        response.print(printStream)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}
