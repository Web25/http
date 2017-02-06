package org.web25.http.handlers

import org.jetbrains.annotations.Contract
import org.web25.http.HttpHandler
import org.web25.http.HttpMiddleware
import java.io.File
import java.io.PrintStream

/**
 * Created by felix on 6/28/16.
 */
object Handlers {

    @Contract("_, _, _ -> !null")
    fun buffered(source: File, caching: Boolean, mimeType: String): HttpHandler {
        return FileHandler.buffered(source, caching, mimeType)
    }

    @Contract("_, _, _ -> !null")
    fun resource(resourceName: String, caching: Boolean, mimeType: String): HttpHandler {
        return FileHandler.resource(mimeType, caching, resourceName)
    }

    @Contract("_, _, _ -> !null")
    fun directory(dir: File, caching: Boolean, cacheTime: Int): HttpHandler {
        return DirectoryFileHandler(dir, caching, cacheTime)
    }

    @Contract("_ -> !null")
    fun debug(printStream: PrintStream): HttpMiddleware {
        return HttpDebugger(printStream)
    }

    @Contract(" -> !null")
    fun log(): HttpMiddleware {
        return LoggingHandler.log()
    }

}
