package org.web25.http.transport

import org.slf4j.LoggerFactory
import org.web25.http.*
import org.web25.http.Http.Methods.GET
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.DefaultHttpResponse
import org.web25.http.drivers.DefaultIncomingHttpRequest
import org.web25.http.drivers.InputBuffer
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse
import org.web25.http.util.HttpCookieHelper
import java.io.*

/**
 * Created by felix on 6/9/16.
 */
class Http11Transport(val context : HttpContext) : org.web25.http.HttpTransport {

    override fun write(httpRequest: OutgoingHttpRequest, outputStream: OutputStream) {
        val output = PrintStream(outputStream)
        val path = httpRequest.path.requestPath()
        output.printf("%s %s %s\r\n", httpRequest.method().toUpperCase(), path, "HTTP/1.1")
        context.cookieStore.findCookies(httpRequest)
        val entity = httpRequest.entity
        if(entity != null) {
            httpRequest.header("Content-Length", entity.getLength())
            if(!httpRequest.hasHeader("Content-Type"))
                httpRequest.header("Content-Type", entity.contentType)
        }
        httpRequest.headers.forEach { name, value ->
            output.printf("%s: %s\r\n", name, value)
        }
        context.cookieStore.findCookies(httpRequest)
        if (httpRequest.cookies.isNotEmpty()) {
            val builder = StringBuilder()
            httpRequest.cookies.forEach { cookie ->
                builder.append(cookie.name)
                builder.append("=")
                builder.append(cookie.value)
                builder.append(";")
            }
            output.printf("%s: %s\r\n", "Cookie", builder.toString())
            output.print("\r\n")
        }
        if (httpRequest.hasEntity) {
            output.print("\r\n")
            val data = httpRequest.entityBytes()
            output.write(data, 0, data.size)
            output.print("\r\n")
        }
        output.print("\r\n")
    }


    override fun write(httpResponse: OutgoingHttpResponse, outputStream: OutputStream, entityStream: InputStream?) {
        val stream = PrintStream(outputStream)
        stream.printf("HTTP/1.1 %03d %s\r\n", httpResponse.statusCode(), httpResponse.status().statusMessage())
        val entity = httpResponse.entity
        if(entity != null) {
            httpResponse.header("Content-Length", entity.getLength().toString())
            if(!httpResponse.hasHeader("Content-Type"))
                httpResponse.header("Content-Type", entity.contentType)
        }
        httpResponse.headers.forEach { name, value ->
            stream.printf("%s: %s\r\n", name, value)
        }
        httpResponse.cookies.forEach { cookie ->
            stream.printf("Set-Cookie: %s\r\n", cookie.toString())
        }
        stream.print("\r\n")
        if (entityStream != null) {
            val buffer = ByteArray(1024)
            var read: Int
            try {
                read = entityStream.read(buffer, 0, 1024)
                while (read > 0) {
                    stream.write(buffer, 0, read)
                    read = entityStream.read(buffer, 0, 1024)
                }
                entityStream.close()
            } catch (e: IOException) {
                log.error("Could not read entity stream", e)
            }

        } else if (httpResponse.responseBytes().isNotEmpty()) {
            stream.write(httpResponse.responseBytes(), 0, httpResponse.responseBytes().size)
            stream.print("\r\n")
        }
    }

    @Throws(IOException::class)
    override fun readRequest(inputStream: InputStream): IncomingHttpRequest {
        val inputBuffer = InputBuffer(inputStream)
        var statusLine = inputBuffer.readUntil('\r'.toByte(), 1)
        val request = DefaultIncomingHttpRequest(context)
        parseRequestLine(statusLine, request)
        statusLine = inputBuffer.readUntil('\r'.toByte(), 1)
        while (statusLine != "") {
            val name: String = statusLine.substring(0, statusLine.indexOf(":")).trim({ it <= ' ' })
            val value: String = statusLine.substring(statusLine.indexOf(":") + 1).trim({ it <= ' ' })
            if (name == "Cookie") {
                HttpCookieHelper.readCookies(value).forEach {
                    request.cookies[it.name] = it
                }
            } else {
                request.header(name, value)
            }
            statusLine = inputBuffer.readUntil('\r'.toByte(), 1)
        }
        if (!request.method().equals(GET, ignoreCase = true) && request.hasHeader("Content-Length")) {
            val length = Integer.parseInt(request.headers["Content-Length"])
            if (length == 0) {
                return request
            }
            val raw = inputBuffer[length]
            request.entity = context.registry.deserialize(raw, request.headers["Content-Type"])
        }
        return request
    }

    @Throws(IOException::class)
    override fun readResponse(inputStream: InputStream, pipe: OutputStream?, request: OutgoingHttpRequest): HttpResponse {
        val inputBuffer = InputBuffer(inputStream)
        var statusLine: String? = inputBuffer.readUntil('\r'.toByte(), 1)
        val response = DefaultIncomingHttpResponse(context)
        response.status(StatusCode.constructFromHttpStatusLine(statusLine!!))
        statusLine = inputBuffer.readUntil('\r'.toByte(), 1)
        while (statusLine != null) {
            if (statusLine == "")
                break
            val name: String = statusLine.substring(0, statusLine.indexOf(":")).trim({ it <= ' ' })
            val value: String = statusLine.substring(statusLine.indexOf(":") + 1).trim({ it <= ' ' })
            if (name == "Set-Cookie") {
                val cookie = HttpCookieHelper.readCookie(value)
                response.cookie(cookie)
            } else {
                response.header(name, value)
            }
            statusLine = inputBuffer.readUntil('\r'.toByte(), 1)
        }
        context.cookieStore.store(request, response)
        if (response.hasHeader("Content-Length") && response.headers["Content-Length"].toInt() > 0) {
            val length = response.headers["Content-Length"].toInt()
            val raw: ByteArray
            if (pipe != null) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                inputBuffer.pipe(length, pipe, byteArrayOutputStream)
                pipe.close()
                raw = byteArrayOutputStream.toByteArray()
            } else {
                raw = inputBuffer[length]
            }
            response.entity = context.registry.deserialize(raw, response.headers["Content-Type"])
        } else {
            log.debug("No content-length received. Treating entity as non existent!")
        }
        return response
    }

    companion object {

        private val log = LoggerFactory.getLogger("HTTP")

        private fun parseRequestLine(line: String, httpRequest: DefaultIncomingHttpRequest) {
            val method = line.substring(0, line.indexOf(" ")).trim({ it <= ' ' })
            val path = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1)).trim({ it <= ' ' })
            val version = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1).trim({ it <= ' ' })
            httpRequest.method(method)
            httpRequest.path(path)
            if (version == "HTTP/1.1") {
                httpRequest.version(HttpVersion.HTTP_11)
            } else if (version == "HTTP/1.0") {
                httpRequest.version(HttpVersion.HTTP_10)
            }
        }
    }
}

class DefaultIncomingHttpResponse(context: HttpContext) : DefaultHttpResponse(context) {

    fun status(statusCode : StatusCode): DefaultIncomingHttpResponse {
        this.statusCode = statusCode
        return this
    }

    fun header(name: String, value: String): DefaultIncomingHttpResponse {
        this.headers[name] = value
        return this
    }


    fun cookie(name: String, value: String): DefaultIncomingHttpResponse {
        this.cookies[name] = value
        return this
    }


    override fun responseBytes(): ByteArray = entity?.getBytes() ?: byteArrayOf()

    fun cookie(cookie: HttpCookie): DefaultIncomingHttpResponse {
        cookies[cookie.name] = cookie
        return this
    }

}
