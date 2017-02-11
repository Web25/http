package org.web25.http.drivers.server

import org.slf4j.LoggerFactory
import org.web25.http.HttpRequest
import org.web25.http.StatusCode
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.server.HttpMiddleware
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

/**
 * Created by felix on 4/25/16.
 */
class HttpHandlerStack {

    private val log = LoggerFactory.getLogger("HTTP")

    private val httpHandlerHandles: MutableList<HttpHandle>

    private val after: MutableList<HttpMiddleware>

    init {
        this.httpHandlerHandles = ArrayList<HttpHandle>()
        this.after = ArrayList<HttpMiddleware>()
    }

    fun submit(httpHandle: HttpHandle) {
        this.httpHandlerHandles.add(httpHandle)
    }

    fun submitAfter(httpMiddleware: HttpMiddleware) {
        after.add(httpMiddleware)
    }

    fun handle(httpRequest: IncomingHttpRequest, httpResponse: OutgoingHttpResponse): Boolean {
        var handled = false
        try {
            for (httpHandle in httpHandlerHandles) {
                if (httpHandle.matches(httpRequest)) {
                    try {
                        if (httpHandle.handle(httpRequest, httpResponse)) {
                            handled = true
                            break
                        }
                    } catch (e: HttpHandleException) {
                        log.warn("Error while handling HTTP request", e)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        e.printStackTrace(PrintStream(byteArrayOutputStream))
                        httpResponse.status(e.statusCode!!)
                        httpResponse.entity(byteArrayOutputStream.toByteArray())
                        httpResponse.header("Content-Type", "text/plain")
                        handled = true
                        break
                    }

                }
            }
        } catch (t: Throwable) {
            log.error("Error while handling " + httpRequest.method() + " " + httpRequest.path(), t)
            val byteArrayOutputStream = ByteArrayOutputStream()
            t.printStackTrace(PrintStream(byteArrayOutputStream))
            httpResponse.entity(byteArrayOutputStream.toByteArray())
            httpResponse.status(StatusCode.INTERNAL_SERVER_ERROR)
        }

        try {
            for (middleware in after) {
                try {
                    middleware(httpRequest, httpResponse)
                } catch (e: HttpHandleException) {
                    log.warn("Error while performing finalizing operations on HTTP request", e)
                }

            }
        } catch (t: Throwable) {
            log.error("Error while finishing " + httpRequest.method() + " " + httpRequest.path(), t)
            val byteArrayOutputStream = ByteArrayOutputStream()
            t.printStackTrace(PrintStream(byteArrayOutputStream))
            httpResponse.entity(byteArrayOutputStream.toByteArray())
            httpResponse.status(StatusCode.INTERNAL_SERVER_ERROR)
        }
        httpResponse.finished = true
        return handled
    }

    fun matches(httpRequest: HttpRequest): Boolean {
        for (httpHandle in httpHandlerHandles) {
            if (httpHandle.matches(httpRequest)) {
                return true
            }
        }
        return false
    }

    fun parentPath(path: String) {
        httpHandlerHandles.forEach { httpHandle -> httpHandle.parentPath(path) }
    }

    fun prependPath(parentPath: String) {
        httpHandlerHandles.forEach { httpHandle -> httpHandle.prependPath(parentPath) }
    }
}
