package org.web25.http

import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.web25.http.auth.Authentication

/**
 * Created by felix on 2/7/16.
 */
internal class HttpsTest {

    val http = Http()
    val parser = JsonParser()

    @Test
    fun testHttpGet() {
        val response = http.get("https://httpbin.org/get").response()
        assertEquals(200, response.status().status())
        assertNotNull(response.responseString())
    }

    @Test
    fun testHttpGetWithParameters() {
        val response = http.get("https://httpbin.org/get?param=2").response()
        assertEquals(200, response.status().status())
        val content = parser.parse(response.responseString()).asJsonObject
        val args = content.getAsJsonObject("args")
        val param = args.get("param")
        assertNotNull(param)
        assertEquals("2", param.asString)
    }

    @Test
    fun testHttpStatusParsing() {
        var response = http.get("https://httpbin.org/status/200").response()
        assertEquals(200, response.statusCode())
        assertEquals("OK", response.status().statusMessage())
        response = http.get("https://httpbin.org/status/404").response()
        assertEquals(404, response.statusCode())
        assertEquals("NOT FOUND", response.status().statusMessage())
        response = http.get("https://httpbin.org/status/500").response()
        assertEquals(500, response.statusCode())
        assertEquals("INTERNAL SERVER ERROR", response.status().statusMessage())
    }

    @Test
    fun testHttpPost() {
        val response = http.get("https://httpbin.org/post").method("POST").response()
        assertEquals(200, response.status().status().toLong())
        assertNotNull(response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithArguments() {
        val response = http.post("https://httpbin.org/post").data("param", "2").response()
        assertEquals(200, response.statusCode())
        val content = parser.parse(response.responseString()).asJsonObject
        val form = content.getAsJsonObject("form")
        val param = form.get("param")
        assertNotNull(param)
        assertEquals("2", param.asString)
    }

    @Test
    fun testHttpPostWithData() {
        val response = http.post("https://httpbin.org/post").entity("Value is 2").contentType("text/plain").response()
        assertEquals(200, response.statusCode())
        val content = parser.parse(response.responseString()).asJsonObject
        val data = content.get("data")
        assertNotNull(data)
        assertEquals("Value is 2", data.asString)
    }

    @Test
    fun testGetWithCookies() {
        val response = http.get("https://httpbin.org/cookies").cookie("Session", "abcd1234").response()
        val content = parser.parse(response.responseString()).asJsonObject
        val cookies = content.getAsJsonObject("cookies")
        val cookie = cookies.get("Session")
        assertNotNull(cookie)
        assertEquals("abcd1234", cookie.asString)
    }

    @Test
    fun testBasicAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.basic({"test"}, {"test"}))
        val response = http.get("https://httpbin.org/basic-auth/test/test").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
    }
}
