package org.web25.http.auth

import org.web25.http.HttpRequest
import org.web25.http.HttpResponse
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.exceptions.HttpException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.bind.DatatypeConverter

/**
 * Created by felix on 6/21/16.
 */
class DefaultDigestStrategy(private val username: () -> String, private val password: () -> String) : Authentication {


    constructor(username: String, password: String) : this({ username }, { password })

    private val digest = object : ThreadLocal<MessageDigest>() {
        override fun initialValue(): MessageDigest {
            try {
                return MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

        }
    }

    private var nonce: String? = null
    private val nc: AtomicInteger = AtomicInteger(1)
    private var realm: String? = null
    private var opaque: String? = null

    private var basePath: String? = null
    private var host: String? = null

    override val isInitialized: Boolean
        get() = nonce != null && realm != null && opaque != null

    override fun matches(request: HttpRequest): Boolean {
        return host!!.equals(request.headers["Host"], ignoreCase = true) && request.path().startsWith(basePath!!)
    }

    override fun supportsMulti(): Boolean {
        return false
    }

    override fun supportsDirect(): Boolean {
        return false
    }

    override fun init(response: HttpResponse) {
        val data = readData(response.headers["WWW-Authenticate"].substring("Digest".length))
        if (!data.containsKey("nonce") && !data.containsKey("opaque") && !data.containsKey("realm")) {
            throw HttpException(response.request(), "Not all required fields for HTTP Digest Authentication are present.")
        }
        this.nonce = removeQuotes(data.getProperty("nonce"))
        this.realm = removeQuotes(data.getProperty("realm"))
        this.opaque = removeQuotes(data.getProperty("opaque"))
        this.host = response.request().headers["Host"]
        this.basePath = response.request().path()
    }

    override fun strategy(): String {
        return "Digest"
    }

    override fun authenticate(request: OutgoingHttpRequest) {
        val stringBuilder = StringBuilder()

        val nc = this.nc.andIncrement
        val cnonce = "0a4f113b" //UUID.randomUUID().toString();

        stringBuilder.append("Digest username=\"")
                .append(username())
                .append("\", realm=\"")
                .append(realm)
                .append("\", nonce=\"")
                .append(nonce)
                .append("\", uri=\"")
                .append(request.path())
                .append("\", qop=auth, nc=")
                .append(String.format("%08x", nc))
                .append(", cnonce=\"")
                .append(cnonce)
                .append("\", response=\"")

        val ha1 = md5(username() + ":" + realm + ":" + password())
        val ha2 = md5(request.method() + ":" + request.path())
        val response = md5(ha1 + ":" + nonce
                + ":" + String.format("%08x", nc) + ":" + cnonce +
                ":auth:" + ha2)

        stringBuilder.append(response)
                .append("\", opaque=\"")
                .append(opaque)
                .append("\"")

        request.header("Authorization", stringBuilder.toString())
    }

    private fun readData(authData: String): Properties {
        val byteArrayInputStream = ByteArrayInputStream(authData.replace(",", "\n").toByteArray())
        val properties = Properties()
        try {
            properties.load(byteArrayInputStream)
        } catch (ignored: IOException) {
        }

        return properties
    }

    private fun md5(data: String): String {
        val digest = this.digest.get()
        digest.reset()
        val res = digest.digest(data.toByteArray())
        return DatatypeConverter.printHexBinary(res).toLowerCase()
    }

    private fun removeQuotes(string: String): String {
        var result = string
        while (result.startsWith("\"")) {
            result = result.substring(1)
        }
        while (result.endsWith("\"")) {
            result = result.substring(0, result.length - 1)
        }
        return result
    }
}
