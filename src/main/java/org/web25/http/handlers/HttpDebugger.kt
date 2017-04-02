package org.web25.http.handlers

import org.slf4j.LoggerFactory
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.HttpMiddleware
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse
import java.io.PrintStream

/**
 * Created by felix on 6/6/16.
 */
class HttpDebugger(private val printStream: PrintStream) : HttpMiddleware {

    init {
        LOGGER.warn("Attention: HTTP Debugging has been activated! This might lead to excessive logging of HTTP Traffic!")
    }

    @Throws(HttpHandleException::class)
    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse) {
        //TODO print debug info
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}
