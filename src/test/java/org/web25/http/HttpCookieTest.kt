package org.web25.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Created by felix on 1/19/16.
 */
internal class HttpCookieTest {

    private var httpCookie: HttpCookie? = null

    @BeforeEach
    fun setUp() {
        httpCookie = HttpCookie("Session", "abcd1234")
    }

    @Test
    fun testConstructor() {
        assertEquals("Session", httpCookie!!.name)
        assertEquals("abcd1234", httpCookie!!.value)
    }

    @Test
    fun testSetName() {
        httpCookie!!.name = "X-Session"
        assertEquals("X-Session", httpCookie!!.name)
    }

    @Test
    fun testSetValue() {
        httpCookie!!.value = "abcd"
        assertEquals("abcd", httpCookie!!.value)
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