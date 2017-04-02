package org.web25.http.entities

import org.web25.http.HttpEntity

/**
 * Created by felix on 3/28/17.
 */
abstract class BufferedEntity<T: Any>(element: T) : HttpEntity {

    var element: T = element
    set(value) {
        field = value
        byteEntity = serialize(value)
    }

    protected var byteEntity: ByteArray = serialize(element)

    abstract fun serialize(element: T): ByteArray

    override fun getLength(): Int = byteEntity.size

    override fun getBytes(): ByteArray = byteEntity
}