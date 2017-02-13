package org.web25.http.handlers

import org.web25.http.HttpRequest
import org.web25.http.StatusCode
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.helper.HttpCacheControl
import org.web25.http.helper.HttpHelper
import org.web25.http.server.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * Created by felix on 6/11/16.
 */
class DirectoryFileHandler(private val parent: File, private val caching: Boolean, private val cacheTime: Int) : HttpRouter {

    lateinit var parentPath: String

    @Throws(HttpHandleException::class)
    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse): Boolean {
        if (!req.path().startsWith(parentPath)) {
            return false
        }
        var path = req.path().replaceFirst(parentPath.toRegex(), "")
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"))
        }
        val file = File(parent, path)
        if (file.exists()) {
            if (caching) {
                if (HttpCacheControl.cacheControl(req, res, cacheTime, java.lang.Long.toHexString(file.lastModified()))) {
                    return true
                }
            }
            try {
                res.header("Content-Length", file.length().toString())
                res.header("Content-Type", HttpHelper.context().mime().contentType(file))
                res.entity(FileInputStream(file))
                return true
            } catch (e: FileNotFoundException) {
                throw HttpHandleException(StatusCode.INTERNAL_SERVER_ERROR, "Could not open resource for read: " + path, e)
            }

        }
        return false
    }

    override fun use(handler: HttpMiddleware): DirectoryFileHandler {
        return this
    }

    override fun use(path: String, handler: HttpMiddleware): DirectoryFileHandler {
        return this
    }

    override fun use(handler: HttpHandler): DirectoryFileHandler {
        return this
    }

    override fun use(path: String, httpHandler: HttpHandler): DirectoryFileHandler {
        return this
    }

    override fun use(method: String, path: String, httpHandler: HttpHandler): DirectoryFileHandler {
        return this
    }

    override fun after(middleware: HttpMiddleware): DirectoryFileHandler {
        return this
    }

    override fun matches(httpRequest: HttpRequest): Boolean {
        if (!httpRequest.path().startsWith(parentPath)) {
            return false
        }
        val path = httpRequest.path().replaceFirst(parentPath.toRegex(), "")
        return File(parent, path).exists()
    }

    override fun prependPath(path: String): HttpRoutable<HttpRouter> {
        this.parentPath = HttpRoutable.joinPaths(path, this.parentPath)
        return this
    }

    override fun parentPath(path: String): HttpRouter {
        this.parentPath = path
        if (this.parentPath.endsWith("/")) {
            this.parentPath = this.parentPath.substring(0, this.parentPath.length - 1)
        }
        return this
    }


}
