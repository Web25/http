package org.web25.http.server

import org.web25.http.HttpContext
import org.web25.http.drivers.client.DefaultHttpRequest

abstract class IncomingHttpRequest(context : HttpContext): DefaultHttpRequest(context) {
    abstract fun checkAuth(username: String, password: String): Boolean
    abstract fun appendBytes(data: ByteArray): IncomingHttpRequest
}