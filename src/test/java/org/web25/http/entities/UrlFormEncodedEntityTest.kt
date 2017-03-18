package org.web25.http.entities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Created by felix on 3/13/17.
 */
class UrlFormEncodedEntityTest {

    @Test
    fun testEntityString() {
        val entity = UrlFormEncodedEntity()
        val uuid = UUID.randomUUID()
        entity["test"] = "test"
        entity["user"] = uuid.toString()
        assertEquals("test=test&user=$uuid", entity.toString())
        println(entity.toString())
    }
}