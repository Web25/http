package org.web25.http.serializer

import com.google.gson.JsonElement
import org.web25.http.entities.BufferedEntity

class JsonEntity(element: JsonElement) : BufferedEntity<JsonElement>(element) {

    override val contentType: String = "application/json"

    override fun serialize(element: JsonElement): ByteArray = element.toString().toByteArray()
}