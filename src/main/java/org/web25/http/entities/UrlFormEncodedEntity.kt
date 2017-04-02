package org.web25.http.entities

/**
 * Created by felix on 3/12/17.
 */
class UrlFormEncodedEntity: AbstractStringableEntity(), MutableMap<String, String> by mutableMapOf() {

    override fun getLength(): Int = getBytes().size

    override val contentType: String = "application/x-www-form-urlencoded"

    override fun getBytes(): ByteArray = map {
        it.key + "=" + it.value
    }.joinToString(separator = "&").toByteArray()
}