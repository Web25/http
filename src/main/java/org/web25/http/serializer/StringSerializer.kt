package org.web25.http.serializer

import org.web25.http.HttpSerializer
import org.web25.http.entities.StringEntity

/**
 * Created by felix on 3/28/17.
 */
class StringSerializer: HttpSerializer<String, StringEntity> {

    override fun serialize(value: String): StringEntity = StringEntity(value)

    override fun deserialize(data: ByteArray): StringEntity = StringEntity(String(data))

    override fun supportsContentType(contentType: String): Boolean = contentType.startsWith("text")

}