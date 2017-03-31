package org.web25.http.server

import org.web25.http.HttpContext
import org.web25.http.drivers.client.DefaultHttpRequest
import org.web25.http.helper.HttpSocketOptions

abstract class IncomingHttpRequest(context : HttpContext, val  httpSocketOptions: HttpSocketOptions = HttpSocketOptions()): DefaultHttpRequest(context) {
    abstract fun appendBytes(data: ByteArray): IncomingHttpRequest
}