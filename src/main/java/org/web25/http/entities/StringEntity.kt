package org.web25.http.entities

/**
 * Created by felix on 3/12/17.
 */
class StringEntity(var value: String): AbstractStringableEntity() {

    override fun getLength(): Int = value.length

    override val contentType: String = "text/plain"

    override fun getBytes(): ByteArray = value.toByteArray()

}