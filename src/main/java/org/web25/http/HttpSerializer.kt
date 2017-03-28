package org.web25.http

/**
 * Serializer to serialize an object to an HttpEntity.
 *
 * @param S the type of object that should be serialized
 * @param E the type of Entity that will be created
 */
interface HttpSerializer<in S: Any, out E: HttpEntity> {

    /**
     * Method used internally to automatically create an entity around any object
     *
     * @param value the value that should be encapsulated
     * @return an entity
     */
    fun serialize(value: S): E

    /**
     * Recreates an entity from bytes
     *
     * @param data the raw data received from the remote peer
     * @return the deserialized entity
     */
    fun deserialize(data: ByteArray): E

    /**
     * Returns whether this serializer can deserialize a given contentType
     *
     * @param contentType the content type sent by the remote peer
     * @return whether the received content type is supported
     */
    fun supportsContentType(contentType: String): Boolean

    companion object {
        fun <S: Any, E: HttpEntity> of(serializer: Serializer<S, E>, deserializer: Deserializer<E>,
                                       contentTypeMatcher: ContentTypeMatcher): HttpSerializer<S, E> {
            return object: HttpSerializer<S, E> {

                override fun serialize(value: S): E = serializer(value)

                override fun deserialize(data: ByteArray): E = deserializer(data)

                override fun supportsContentType(contentType: String): Boolean = contentTypeMatcher(contentType)

            }
        }
    }

}

typealias Serializer<S, E> = (value: S) -> E
typealias Deserializer<E> = (data: ByteArray) -> E
typealias ContentTypeMatcher = (contentType: String) -> Boolean