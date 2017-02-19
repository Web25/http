package org.web25.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


/**
 * Created by felix on 9/10/15.
 */
internal class StatusCodeTest {

    @Test
    fun testIndex() {
        assertEquals(200, StatusCode.OK.status())
    }

    @Test
    fun testFind() {
        assertEquals(200, StatusCode.find(200).status())
        assertEquals(404, StatusCode.find(404).status())
    }

    @Test
    fun testConstructHttpStatus() {
        val statusCode = StatusCode.constructFromHttpStatusLine("HTTP/1.1 200 OK")
        assertEquals(200, statusCode.status())
        assertEquals("OK", statusCode.statusMessage())
    }
}