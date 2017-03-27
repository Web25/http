package org.web25.http

interface HttpSerializer<in S: Any, out E: HttpEntity> {

    fun serialize(value: S): E
    fun deserialize(data: ByteArray): E
    fun supportsContentType(contentType: String): Boolean
}