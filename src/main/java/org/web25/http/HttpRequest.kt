package org.web25.http

import org.web25.http.events.HttpEventHandler
import org.web25.http.events.HttpEventManager
import org.web25.http.events.HttpEventType
import java.io.OutputStream
import java.util.function.Supplier

/**
 * Created by felix on 9/10/15.
 */
abstract class HttpRequest {

    abstract fun method(method: String): HttpRequest
    abstract fun cookie(name: String, value: String): HttpRequest
    abstract fun header(name: String, value: String): HttpRequest
    abstract fun entity(entity: ByteArray): HttpRequest
    abstract fun entity(entity: String): HttpRequest
    abstract fun entity(entity: Any): HttpRequest
    abstract fun execute(callback: HttpResponseCallback?): HttpRequest
    abstract fun transport(transport: Transport): HttpRequest
    abstract fun version(version: HttpVersion): HttpRequest
    abstract fun print(outputStream: OutputStream): HttpRequest
    abstract fun data(key: String, value: String): HttpRequest
    abstract fun eventManager(manager: HttpEventManager): HttpRequest
    abstract fun event(type: HttpEventType, handler: HttpEventHandler): HttpRequest

    abstract fun using(driver: Driver): HttpRequest

    abstract fun pipe(outputStream: OutputStream): HttpRequest

    abstract fun prepareEntity(): HttpRequest

    abstract fun method(): String
    abstract fun cookies(): Collection<HttpCookie>
    abstract fun headers(): Collection<HttpHeader>
    abstract fun entityBytes(): ByteArray
    abstract fun entityString(): String
    abstract fun checkAuth(username: String, password: String): Boolean
    abstract fun response(): HttpResponse
    abstract fun use(httpTransport: HttpTransport): HttpRequest
    abstract fun port(port: Int): HttpRequest
    abstract fun host(host: String): HttpRequest
    abstract fun path(path: String): HttpRequest

    abstract fun transport(): Transport
    abstract fun requestLine(): String

    fun execute(): HttpRequest {
        return execute(null)
    }

    abstract fun header(name: String): HttpHeader
    abstract fun hasHeader(name: String): Boolean

    fun hasHeaders(vararg names: String): Boolean {
        for (name in names) {
            if (!hasHeader(name))
                return false
        }
        return true
    }


    fun contentType(contentType: String): HttpRequest {
        return header("Content-Type", contentType)
    }

    abstract fun basicAuth(username: () -> String, password: () -> String): HttpRequest
    abstract fun basicAuth(username: Supplier<String>, password: Supplier<String>): HttpRequest

    fun basicAuth(username: String, password: String): HttpRequest {
        return basicAuth({ username }, { password })
    }

    abstract fun <T : Driver> drivers(type: Class<T>): List<T>

    fun https(): HttpRequest {
        return transport(Transport.HTTPS)
    }

    abstract fun path(): String

    abstract fun hasCookie(name: String): Boolean
}
