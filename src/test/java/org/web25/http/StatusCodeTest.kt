package org.web25.http

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by felix on 9/10/15.
 */
class StatusCodeTest {

    @Test
    @Throws(Exception::class)
    fun testIndex() {
        assertEquals("Status OK", 200, StatusCode.OK.status())
    }

    @Test
    @Throws(Exception::class)
    fun testFind() {
        assertEquals("Status OK", 200, StatusCode.find(200).status())
        assertEquals("Status Not Found", 404, StatusCode.find(404).status())
    }

    @Test
    @Throws(Exception::class)
    fun testConstructHttpStatus() {
        val statusCode = StatusCode.constructFromHttpStatusLine("HTTP/1.1 200 OK")
        assertEquals("Status code", 200, statusCode.status())
        assertEquals("Status Message", "OK", statusCode.statusMessage())
    }
}