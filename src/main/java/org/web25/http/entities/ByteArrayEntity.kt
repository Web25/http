package org.web25.http.entities

/**
 * Created by felix on 3/15/17.
 */
class ByteArrayEntity(var data: ByteArray): AbstractStringableEntity() {

    override fun getLength(): Int = data.size

    override val contentType: String = "application/octet-stream"

    override fun getBytes(): ByteArray = data
}