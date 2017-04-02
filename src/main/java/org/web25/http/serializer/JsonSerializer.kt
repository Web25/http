package org.web25.http.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.web25.http.HttpSerializer
import org.web25.http.entities.JsonEntity
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/**
 * Created by felix on 3/28/17.
 */
class JsonSerializer: HttpSerializer<JsonElement, JsonEntity> {

    private val jsonParser: JsonParser by lazy {
        JsonParser()
    }

    override fun serialize(value: JsonElement): JsonEntity = JsonEntity(value)

    override fun deserialize(data: ByteArray): JsonEntity {
        return JsonEntity(jsonParser.parse(InputStreamReader(ByteArrayInputStream(data))))
    }

    override fun supportsContentType(contentType: String): Boolean = contentType.startsWith("application/json")
}

