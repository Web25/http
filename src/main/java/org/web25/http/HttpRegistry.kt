package org.web25.http

/**
 * Created by felix on 3/18/17.
 */
interface HttpRegistry {

    fun addSerializer(serializer: HttpSerializer<*, *>)
    fun <T: Any> serialize(value: T): HttpEntity
    fun deserialize(data: ByteArray, contentType: String): HttpEntity
}

