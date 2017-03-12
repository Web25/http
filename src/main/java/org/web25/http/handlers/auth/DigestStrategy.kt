package org.web25.http.handlers.auth

import org.web25.http.HttpRequest
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.xml.bind.DatatypeConverter

/**
 * Created by felix on 6/13/16.
 */
class DigestStrategy(private val realm: String, private val credentialProvider: CredentialProvider, private val nonceManager: NonceManager) : Strategy {

    private val digest = object : ThreadLocal<MessageDigest>() {
        override fun initialValue(): MessageDigest {
            try {
                return MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

        }
    }

    override fun authenticate(request: HttpRequest): Boolean {
        if (!request.hasHeader("Authorization")) {
            return false
        }
        val authorization = request.headers["Authorization"]
        if (!authorization.startsWith(name())) {
            return false
        }
        val authData = authorization.substring(authorization.indexOf(" ") + 1)
        val properties = readData(authData)
        val user = removeQuotes(properties.getProperty("username"))
        val credentials = credentialProvider.findByUsername(user) ?: return false
        val nonce = removeQuotes(properties.getProperty("nonce"))
        val nc = properties.getProperty("nc")
        if (!nonceManager.verifyAndUpdate(nonce, nc)) {
            return false
        }
        val ha1 = md5(user + ":" +
                removeQuotes(properties.getProperty("realm")) + ":" +
                credentials.password)
        val ha2 = md5(request.method() + ":" + request.path())
        val response = md5(ha1 + ":" + removeQuotes(properties.getProperty("nonce"))
                + ":" + properties.getProperty("nc") + ":" + removeQuotes(properties.getProperty("cnonce")) +
                ":" + properties.getProperty("qop") + ":" + ha2)
        return response == removeQuotes(properties.getProperty("response"))
    }

    override fun name(): String {
        return "Digest"
    }

    override fun realm(): String {
        return realm
    }

    override fun authenticateHeader(): String {
        val nonce: String = nonceManager.generateNew()
        return "${name()} realm=\"$realm\", qop=\"auth\", nonce=\"$nonce\", opaque=\"${nonceManager.getOpaque(nonce)}\""
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
        var string = string
        while (string.startsWith("\"")) {
            string = string.substring(1)
        }
        while (string.endsWith("\"")) {
            string = string.substring(0, string.length - 1)
        }
        return string
    }
}
