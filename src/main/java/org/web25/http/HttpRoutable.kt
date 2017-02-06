package org.web25.http

import java.util.function.Supplier

/**
 * Created by Felix Resch on 29-Apr-16.
 */
interface HttpRoutable<out T: HttpRoutable<T>> {

    fun use(handler: HttpMiddleware): T


    fun use(path: String, handler: HttpMiddleware): T


    fun use(handler: HttpHandler): T

    fun use(path: String, httpHandler: HttpHandler): T

    fun use(method: String, path: String, httpHandler: HttpHandler): T

    fun after(middleware: HttpMiddleware): T

    operator fun get(path: String, httpHandler: HttpHandler): T {
        return use(Http.GET, path, httpHandler)
    }

    operator fun get(path: String, httpHandler: Supplier<HttpHandler>): T {
        return use(Http.GET, path, httpHandler.get())
    }

    fun post(path: String, httpHandler: HttpHandler): T {
        return use(Http.POST, path, httpHandler)
    }

    fun post(path: String, httpHandler: Supplier<HttpHandler>): T {
        return use(Http.POST, path, httpHandler.get())
    }

    fun put(path: String, httpHandler: HttpHandler): T {
        return use(Http.PUT, path, httpHandler)
    }

    fun put(path: String, httpHandler: Supplier<HttpHandler>): T {
        return use(Http.PUT, path, httpHandler.get())
    }

    fun delete(path: String, httpHandler: HttpHandler): T {
        return use(Http.DELETE, path, httpHandler)
    }

    fun delete(path: String, httpHandler: Supplier<HttpHandler>): T {
        return use(Http.DELETE, path, httpHandler.get())
    }

    fun update(path: String, httpHandler: HttpHandler): T {
        return use(Http.UPDATE, path, httpHandler)
    }

    fun update(path: String, httpHandler: Supplier<HttpHandler>): T {
        return use(Http.UPDATE, path, httpHandler.get())
    }

    fun patch(path: String, httpHandler: HttpHandler): T {
        return use(Http.PATCH, path, httpHandler)
    }

    fun patch(path: String, httpHandler: Supplier<HttpHandler>): T {
        return use(Http.PATCH, path, httpHandler.get())
    }

    fun matches(httpRequest: HttpRequest): Boolean

    fun prependPath(path: String): HttpRoutable<T>

    companion object {

        fun joinPaths(path1: String?, path2: String?): String {
            var path1 = path1
            var path2 = path2
            if (path1 == null) {
                path1 = ""
            } else if (path1.endsWith("/")) {
                path1 = path1.substring(0, path1.length - 1)
            }
            if (path2 == null) {
                path2 = ""
            } else if (path2.startsWith("/")) {
                path2 = path2.substring(1)
            }
            return arrayOf(path1, path2).joinToString("/")
        }
    }
}
