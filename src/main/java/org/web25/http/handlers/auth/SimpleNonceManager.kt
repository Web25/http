package org.web25.http.handlers.auth

import org.slf4j.LoggerFactory
import java.util.*

/**
 * Created by felix on 6/13/16.
 */
class SimpleNonceManager : NonceManager {
    init {
        LOGGER.warn("You are using an unsafe basic implementation of NonceManager. Please fallback a database backed one in production environments for security reasons.")
    }

    override fun generateNew(): String {
        return UUID.randomUUID().toString()
    }

    override fun getOpaque(nonce: String): String {
        return UUID.randomUUID().toString()
    }

    override fun verifyAndUpdate(nonce: String, nc: String): Boolean {
        return true
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}
