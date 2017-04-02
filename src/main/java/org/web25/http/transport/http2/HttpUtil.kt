package org.web25.http.transport.http2

import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/**
 * Created by felix on 9/3/16.
 */
object HttpUtil {

    private val log = LoggerFactory.getLogger("HTTP/2.0")

    fun toByte(data: Int): ByteArray {
        val byteBuffer = ByteBuffer.allocate(Integer.BYTES)
        byteBuffer.putInt(data)
        byteBuffer.flip()
        return byteBuffer.array()
    }

    fun toByte(data: Short): ByteArray {
        val byteBuffer = ByteBuffer.allocate(java.lang.Short.BYTES)
        byteBuffer.putShort(data)
        byteBuffer.flip()
        return byteBuffer.array()
    }

    fun toInt(bytes: ByteArray): Int {
        val byteBuffer = ByteBuffer.allocate(Integer.BYTES)
        if (bytes.size < Integer.BYTES) {
            byteBuffer.position(Integer.BYTES - bytes.size)
        }
        byteBuffer.put(bytes)
        byteBuffer.flip()
        return byteBuffer.int
    }

    fun toShort(bytes: ByteArray): Short {
        val byteBuffer = ByteBuffer.allocate(java.lang.Short.BYTES)
        if (bytes.size < java.lang.Short.BYTES) {
            byteBuffer.position(java.lang.Short.BYTES - bytes.size)
        }
        byteBuffer.put(bytes)
        byteBuffer.flip()
        return byteBuffer.short
    }

    fun containsUppercase(text: String): Boolean {
        for (c in text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true
            }
        }
        return false
    }
}
