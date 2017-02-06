package org.web25.http

/**
 * Created by Felix Resch on 08-Apr-16.
 */
interface Base64Driver : Driver {

    fun encodeToString(data: ByteArray): String
    fun decodeFromString(data: String): ByteArray
}
