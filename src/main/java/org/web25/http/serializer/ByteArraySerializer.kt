package org.web25.http.serializer

import org.web25.http.HttpSerializer
import org.web25.http.entities.ByteArrayEntity

class ByteArraySerializer: HttpSerializer<ByteArray, ByteArrayEntity> {

    override fun serialize(value: ByteArray): ByteArrayEntity {
        return ByteArrayEntity(value)
    }

    override fun deserialize(data: ByteArray): ByteArrayEntity {
        return ByteArrayEntity(data)
    }

    override fun supportsContentType(contentType: String): Boolean = true

}