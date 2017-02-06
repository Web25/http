package org.web25.http.handlers.auth

/**
 * Created by felix on 6/13/16.
 */
interface NonceManager {
    fun generateNew(): String

    fun getOpaque(nonce: String): String

    fun verifyAndUpdate(nonce: String, nc: String): Boolean
}
