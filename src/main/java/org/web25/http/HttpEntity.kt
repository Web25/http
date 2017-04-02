package org.web25.http

/**
 * Class to represent an HTTP Entity (basically the body of a message)
 *
 * @author Felix Resch <felix.resch@web25.org>
 * @since 0.2.0
 */
interface HttpEntity {

    /**
     * The MIME type of the http entity
     */
    val contentType: String

    /**
     * The length of the entity, if not possible to calculate, an implementation should throw an exception
     */
    fun getLength(): Int

    /**
     * Returns a binary representation of the entity.
     * @return a binary representation of the entity.
     */
    fun getBytes(): ByteArray
}