package org.web25.http.helper

import org.jetbrains.annotations.Contract
import org.web25.http.Driver
import org.web25.http.HttpContext
import org.web25.http.HttpRequest
import org.web25.http.HttpResponse
import org.web25.http.drivers.server.HttpThread
import org.xjs.dynamic.Pluggable
import java.net.SocketAddress

/**
 * Created by Felix Resch on 29-Apr-16.
 */
object HttpHelper {

    private val context = object : ThreadLocal<HttpContext>() {
        @Contract(" -> !null")
        override fun initialValue(): HttpContext {
            return DefaultHttpContext()
        }
    }

    fun response(): HttpResponse {
        if (Thread.currentThread() is Pluggable<*>) {
            val httpResponse = (Thread.currentThread() as Pluggable<*>).getFirst<HttpResponse>(HttpResponse::class.java)
            if (httpResponse.isPresent) {
                return httpResponse.get()
            }
        }
        throw RuntimeException("No Response found on current thread!")
    }

    fun request(): HttpRequest {
        if (Thread.currentThread() is Pluggable<*>) {
            val httpRequest = (Thread.currentThread() as Pluggable<*>).getFirst<HttpRequest>(HttpRequest::class.java)
            if (httpRequest.isPresent) {
                return httpRequest.get()
            }
        }
        throw RuntimeException("No Request found on current thread!")
    }

    fun response(response: HttpResponse) {
        if (Thread.currentThread() is Pluggable<*>) {
            val Pluggable = Thread.currentThread() as Pluggable<*>
            Pluggable.removeAll(HttpResponse::class.java)
            Pluggable.add(response)
        }
    }

    fun request(request: HttpRequest) {
        if (Thread.currentThread() is Pluggable<*>) {
            val Pluggable = Thread.currentThread() as Pluggable<*>
            Pluggable.removeAll(HttpRequest::class.java)
            Pluggable.add(request)
        }
    }

    fun remote(): SocketAddress {
        if (Thread.currentThread() is Pluggable<*>) {
            val socketAddress = (Thread.currentThread() as Pluggable<*>).getFirst(SocketAddress::class.java)
            if (socketAddress.isPresent()) {
                return socketAddress.get()
            }
        }
        throw RuntimeException("No Remote Address found on current thread!")
    }

    fun remote(socketAddress: SocketAddress) {
        if (Thread.currentThread() is Pluggable<*>) {
            val Pluggable = Thread.currentThread() as Pluggable<*>
            Pluggable.removeAll(SocketAddress::class.java)
            Pluggable.add(socketAddress)
        }
    }

    fun context(): HttpContext {
        return context.get()
    }

    fun useDriver(driver: Driver) {
        context.get().useDriver(driver)
    }

    fun get(): Pluggable<HttpThread> {
        return Thread.currentThread() as Pluggable<HttpThread>
    }

    fun keepOpen() {
        if (Thread.currentThread() is Pluggable<*>) {
            ((Thread.currentThread() as Pluggable<*>).getFirst(HttpSocketOptions::class.java).get() as HttpSocketOptions).isClose = false
        }
    }

    fun callback(handledCallback: HandledCallback) {
        if (Thread.currentThread() is Pluggable<*>) {
            ((Thread.currentThread() as Pluggable<*>).getFirst(HttpSocketOptions::class.java).get() as HttpSocketOptions).setHandledCallback(handledCallback)
        }
    }
}
