package org.web25.http

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by felix on 1/19/16.
 */
class HttpCookieTest {

    private var httpCookie: HttpCookie? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        httpCookie = HttpCookie("Session", "abcd1234")
    }

    @Test
    @Throws(Exception::class)
    fun testConstructor() {
        assertEquals("Cookie Name", "Session", httpCookie!!.name)
        assertEquals("Cookie Value", "abcd1234", httpCookie!!.value)
    }

    @Test
    @Throws(Exception::class)
    fun testSetName() {
        httpCookie!!.name = "X-Session"
        assertEquals("Cookie Name", "X-Session", httpCookie!!.name)
    }

    @Test
    @Throws(Exception::class)
    fun testSetValue() {
        httpCookie!!.value = "abcd"
        assertEquals("Cookie Value", "abcd", httpCookie!!.value)
    }
}