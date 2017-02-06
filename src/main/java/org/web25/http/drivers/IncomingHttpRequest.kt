package org.web25.http.drivers


/**
 * Created by felix on 2/25/16.
 */
open class IncomingHttpRequest() : DefaultHttpRequest() {

    fun appendBytes(data: ByteArray): IncomingHttpRequest {
        val contentLength = if (hasHeader("Content-Length")) header("Content-Length").value else null
        if (entityBytes().isNotEmpty()) {
            entity(data)
        } else {
            val payload = entityBytes()
            val dst = ByteArray(payload.size + data.size)
            System.arraycopy(dst, 0, payload, 0, payload.size)
            System.arraycopy(dst, payload.size, data, 0, data.size)
            entity(dst)
        }
        if (contentLength != null)
            header("Content-Length", contentLength)
        return this
    }
}
