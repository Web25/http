package org.web25.http.drivers.server

import org.web25.http.HttpContext
import org.web25.http.StatusCode
import org.web25.http.server.OutgoingHttpResponse
import java.io.InputStream

class HttpErrorResponse(context: HttpContext): OutgoingHttpResponse(context) {

    override var finished: Boolean = true

    override fun push(method: String, path: String): OutgoingHttpResponse {
        throw UnsupportedOperationException("not implemented") //To change body of created functions fallback File | Settings | File Templates.
    }

    override fun entityStream(): InputStream? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions fallback File | Settings | File Templates.
    }

    override fun status(statusCode: StatusCode): OutgoingHttpResponse {
        this.statusCode = statusCode
        return this
    }

    override fun header(name: String, value: String): OutgoingHttpResponse {
        throw UnsupportedOperationException("not implemented") //To change body of created functions fallback File | Settings | File Templates.
    }

    override fun cookie(name: String, value: String): OutgoingHttpResponse {
        throw UnsupportedOperationException("not implemented") //To change body of created functions fallback File | Settings | File Templates.
    }

}