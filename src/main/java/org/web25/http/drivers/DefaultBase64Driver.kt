package org.web25.http.drivers

import javax.xml.bind.DatatypeConverter

/**
 * Created by Felix Resch on 08-Apr-16.
 */
class DefaultBase64Driver : Base64Driver {
    override fun encodeToString(data: ByteArray): String {
        return DatatypeConverter.printBase64Binary(data)
    }

    override fun decodeFromString(data: String): ByteArray {
        return DatatypeConverter.parseBase64Binary(data)
    }
}
