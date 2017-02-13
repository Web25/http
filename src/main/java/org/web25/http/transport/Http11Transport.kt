package org.web25.http.transport

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.web25.http.*
import org.web25.http.Http.Methods.GET
import org.web25.http.drivers.DefaultHttpResponse
import org.web25.http.drivers.DefaultIncomingHttpRequest
import org.web25.http.drivers.InputBuffer
import org.web25.http.server.IncomingHttpRequest
import java.io.*

/**
 * Created by felix on 6/9/16.
 */
class Http11Transport(val context : HttpContext) : org.web25.http.HttpTransport {

    override fun write(httpRequest: HttpRequest, outputStream: OutputStream) {
        val output = PrintStream(outputStream)
        httpRequest.prepareEntity()
        output.printf("%s %s %s\r\n", httpRequest.method().toUpperCase(), httpRequest.path() /*+ if (httpRequest.openRequest().query != null) "?" + httpRequest.openRequest().query else ""*/, "HTTP/1.1")
        for (header in httpRequest.headers.values) {
            output.printf("%s: %s\r\n", header.name, header.value)
        }
        if (httpRequest.cookies.isNotEmpty()) {
            val builder = StringBuilder()
            for ((name, value) in httpRequest.cookies.values) {
                builder.append(name)
                builder.append("=")
                builder.append(value)
                builder.append(";")
            }
            output.printf("%s: %s\r\n", "Cookie", builder.toString())
            output.print("\r\n")
        }
        if (httpRequest.entityBytes().isNotEmpty()) {
            output.print("\r\n")
            output.write(httpRequest.entityBytes(), 0, httpRequest.entityBytes().size)
            output.print("\r\n")
        }
        output.print("\r\n")
    }


    override fun write(httpResponse: HttpResponse, outputStream: OutputStream, entityStream: InputStream?) {
        val stream = PrintStream(outputStream)
        stream.printf("HTTP/1.1 %03d %s\r\n", httpResponse.statusCode(), httpResponse.status().statusMessage())
        for (header in httpResponse.headers.values) {
            stream.printf("%s: %s\r\n", header.name, header.value)
        }
        for ((name, value) in httpResponse.cookies) {
            stream.printf("Set-Cookie: %s\r\n", value)
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
                val cname: String = value.substring(0, value.indexOf("="))
                val cvalue: String = value.substring(value.indexOf("=") + 1, if (!value.contains(";")) value.length else value.indexOf(";"))
                request.cookie(cname, cvalue)
            } else {
                request.header(name, value)
            }
            statusLine = inputBuffer.readUntil('\r'.toByte(), 1)
        }
        if (!request.method().equals(GET, ignoreCase = true) && request.hasHeader("Content-Length")) {
            val length = Integer.parseInt(request.header("Content-Length").value)
            if (length == 0) {
                return request
            }
            request.entity(inputBuffer[length])
        }
        return request
    }

    @Throws(IOException::class)
    override fun readResponse(inputStream: InputStream, pipe: OutputStream?): HttpResponse {
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
                val cname: String = value.substring(0, value.indexOf("="))
                val cvalue: String = value.substring(value.indexOf("=") + 1, if (!value.contains(";")) value.length else value.indexOf(";"))
                response.cookie(cname, cvalue)
            } else {
                response.header(name, value)
            }
            statusLine = inputBuffer.readUntil('\r'.toByte(), 1)
        }
        if (response.hasHeader("Content-Length")) {
            val length = response.header("Content-Length").asInt()
            if (pipe != null) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                inputBuffer.pipe(length, pipe, byteArrayOutputStream)
                pipe.close()
                response.entity(byteArrayOutputStream.toByteArray())
            } else {
                response.entity(inputBuffer[length])
            }
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

    private val log = LoggerFactory.getLogger("HTTP")

    private var entityStream: InputStream? = null

    fun status(statusCode : StatusCode): DefaultIncomingHttpResponse {
        this.statusCode = statusCode
        return this
    }

    fun header(name: String, value: String): DefaultIncomingHttpResponse {
        this.headers.put(name, HttpHeader(name, value))
        return this
    }


    fun cookie(name: String, value: String): DefaultIncomingHttpResponse {
        this.cookies.put(name, HttpCookie(name, value))
        return this
    }

    fun entity(entity: ByteArray): DefaultIncomingHttpResponse {
        header("Content-Length", entity.size.toString())
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entity = entity
        return this
    }

    fun entity(inputStream: InputStream): DefaultIncomingHttpResponse {
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entityStream = inputStream
        return this
    }

    override fun responseBytes(): ByteArray {
        if (request().method().equals("HEAD", ignoreCase = true)) {
            return byteArrayOf()
        } else if (entity.isEmpty() && entityStream != null) {
            try {
                this.entity = IOUtils.toByteArray(entityStream!!)
            } catch (e: IOException) {
                log.warn("Could not read resource", e)
            }

        }
        return entity
    }

}
