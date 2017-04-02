package org.web25.http.handlers

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.jetbrains.annotations.Contract
import org.web25.http.Http
import org.web25.http.StatusCode
import org.web25.http.drivers.Base64Driver
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.helper.HttpCacheControl
import org.web25.http.server.HttpHandler
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse
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
    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse): Boolean {
        if (req.method() == Http.Methods.GET) {
            if (!(caching && HttpCacheControl.cacheControl(req, res, 3600, etag(req.context.base64())))) {
                res.header("Content-Type", mimeType).entity(fileContent(req.context.base64()))
            }
            return true
        } else {
            res.status(StatusCode.METHOD_NOT_ALLOWED)
                    .entity("Method " + req.method().toUpperCase() + " not allowed")
                    .header("Accept", Http.Methods.GET)
            return false
        }
    }

    @Throws(HttpHandleException::class)
    protected abstract fun fileContent(base64Driver: Base64Driver): ByteArray

    @Throws(HttpHandleException::class)
    protected abstract fun etag(base64Driver: Base64Driver): String

    private class BufferedFileHandler(caching: Boolean, private val source: File, mimeType: String) : FileHandler(caching, mimeType) {
        lateinit var buffer: ByteArray
        var lastModified: Long = source.lastModified()
        lateinit var etag: String

        @Throws(HttpHandleException::class)
        override fun fileContent(base64Driver: Base64Driver): ByteArray {
            validateBuffer()
            return buffer
        }

        @Throws(HttpHandleException::class)
        override fun etag(base64Driver: Base64Driver): String {
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
        override fun fileContent(base64Driver: Base64Driver): ByteArray {
            validateBuffer(base64Driver)
            return buffer
        }

        @Throws(HttpHandleException::class)
        override fun etag(base64Driver: Base64Driver): String {
            validateBuffer(base64Driver)
            return etag
        }

        @Throws(HttpHandleException::class)
        private fun validateBuffer(base64Driver: Base64Driver) {
            if (buffer.isEmpty()) {
                try {
                    val messageDigest = MessageDigest.getInstance("MD5")
                    val digestInputStream = DigestInputStream(javaClass.getResourceAsStream(resourceName), messageDigest)
                    buffer = IOUtils.toByteArray(digestInputStream)
                    etag = base64Driver.encodeToString(messageDigest.digest())
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
