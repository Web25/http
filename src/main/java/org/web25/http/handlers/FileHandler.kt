package org.web25.http.handlers

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.jetbrains.annotations.Contract
import org.web25.http.*
import org.web25.http.helper.HttpCacheControl
import org.web25.http.helper.HttpHelper
import java.io.File
import java.io.IOException
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by felix on 6/6/16.
 */
abstract class FileHandler private constructor(private val caching: Boolean, private val mimeType: String) : HttpHandler {

    @Throws(HttpHandleException::class)
    override fun handle(request: HttpRequest, response: HttpResponse): Boolean {
        if (request.method() == Http.GET) {
            if (!(caching && HttpCacheControl.cacheControl(request, response, 3600, etag()))) {
                response.header("Content-Type", mimeType).entity(fileContent())
            }
            return true
        } else {
            response.status(StatusCode.METHOD_NOT_ALLOWED)
                    .entity("Method " + request.method().toUpperCase() + " not allowed")
                    .header("Accept", Http.GET)
            return false
        }
    }

    @Throws(HttpHandleException::class)
    protected abstract fun fileContent(): ByteArray

    @Throws(HttpHandleException::class)
    protected abstract fun etag(): String

    private class BufferedFileHandler(caching: Boolean, private val source: File, mimeType: String) : FileHandler(caching, mimeType) {
        lateinit var buffer: ByteArray
        var lastModified: Long = source.lastModified()
        lateinit var etag: String

        @Throws(HttpHandleException::class)
        override fun fileContent(): ByteArray {
            validateBuffer()
            return buffer
        }

        @Throws(HttpHandleException::class)
        override fun etag(): String {
            validateBuffer()
            return etag
        }

        @Throws(HttpHandleException::class)
        private fun validateBuffer() {
            if (!source.exists()) {
                throw HttpHandleException(StatusCode.NOT_FOUND, "The server could not find the resource you were looking for")
            }
            if (lastModified < source.lastModified()) {
                lastModified = source.lastModified()
                this.etag = java.lang.Long.toHexString(lastModified)
                if (source.length() > Integer.MAX_VALUE) {
                    throw RuntimeException("Resource " + source.name + " can not be buffered because it exceeds the maximum buffer size!")
                }
                try {
                    buffer = FileUtils.readFileToByteArray(source)
                } catch (e: IOException) {
                    throw HttpHandleException(StatusCode.INTERNAL_SERVER_ERROR, "Server is not capable to create the requested resource", e)
                }

            }
        }
    }

    class ResourceFileHandler(caching: Boolean, mimeType: String, private val resourceName: String) : FileHandler(caching, mimeType) {
        var buffer: ByteArray = byteArrayOf()
        lateinit var etag: String

        @Throws(HttpHandleException::class)
        override fun fileContent(): ByteArray {
            validateBuffer()
            return buffer
        }

        @Throws(HttpHandleException::class)
        override fun etag(): String {
            validateBuffer()
            return etag
        }

        @Throws(HttpHandleException::class)
        private fun validateBuffer() {
            if (buffer.isEmpty()) {
                try {
                    val messageDigest = MessageDigest.getInstance("MD5")
                    val digestInputStream = DigestInputStream(javaClass.getResourceAsStream(resourceName), messageDigest)
                    buffer = IOUtils.toByteArray(digestInputStream)
                    etag = HttpHelper.context().base64().encodeToString(messageDigest.digest())
                } catch (e: IOException) {
                    throw HttpHandleException(StatusCode.INTERNAL_SERVER_ERROR, "Server is not capable to create the requested resource", e)
                } catch (e: NoSuchAlgorithmException) {
                    throw HttpHandleException(StatusCode.INTERNAL_SERVER_ERROR, "Server is not capable to create the requested resource", e)
                }

            }
        }
    }

    companion object {

        @Contract("_, _, _ -> !null")
        fun buffered(source: File, caching: Boolean, mimeType: String): FileHandler {
            return BufferedFileHandler(caching, source, mimeType)
        }

        @Contract("_, _, _ -> !null")
        fun resource(resourceName: String, caching: Boolean, mimeType: String): FileHandler {
            return ResourceFileHandler(caching, mimeType, resourceName)
        }
    }
}
