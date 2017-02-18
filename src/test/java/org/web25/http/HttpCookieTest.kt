package org.web25.http

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

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

    @Test
    fun testSimpleCookie() {
        val cookie = HttpCookie("SID", "31d4d96e407aad42")
        assertEquals("SID=31d4d96e407aad42", cookie.toString())
    }

    @Test
    fun testCookieWithPathAndDomain() {
        val cookie = HttpCookie("SID", "31d4d96e407aad42", path = "/", domain = "example.com")
        assertEquals("SID=31d4d96e407aad42; Domain=example.com; Path=/", cookie.toString())
    }

    @Test
    fun testSecureHttpOnly() {
        val cookie = HttpCookie("SID", "31d4d96e407aad42", secure = true, httpOnly = true, path = "/")
        assertEquals("SID=31d4d96e407aad42; Path=/; Secure; HttpOnly", cookie.toString())
    }

    @Test
    fun testExpires() {
        val cookie = HttpCookie("lang", "en-US", expires = ZonedDateTime.of(2021, 6, 9, 10, 18, 14, 0, ZoneId.of("GMT")))
        assertEquals("lang=en-US; Expires=Wed, 9 Jun 2021 10:18:14 GMT", cookie.toString())         //RFC 1123 does not require a 2 digit day of month
    }
}