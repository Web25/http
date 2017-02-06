package org.web25.http

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import org.web25.http.drivers.DefaultDriver
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Created by felix on 2/7/16.
 */
class HttpsTest {

    @Rule
    var timeout: TestRule = DisableOnDebug(Timeout(30, TimeUnit.SECONDS))

    @Test
    @Throws(Exception::class)
    fun testHttpGet() {
        val response = Http["https://httpbin.org/get"].response()
        assertEquals("Status", 200, response.status().status().toLong())
        assertNotNull("Response String", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpGetWithParameters() {
        val response = Http[URL("https://httpbin.org/get?param=2")].response()
        assertEquals("Status", 200, response.status().status().toLong())
        val content = parser!!.parse(response.responseString()).asJsonObject
        val args = content.getAsJsonObject("args")
        val param = args.get("param")
        assertNotNull("Param", param)
        assertEquals("Param Value", "2", param.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testHttpStatusParsing() {
        var response = Http["https://httpbin.org/status/200"].response()
        assertEquals("HTTP OK Statuscode", 200, response.statusCode().toLong())
        assertEquals("HTTP OK Status", "OK", response.status().statusMessage())
        response = Http["https://httpbin.org/status/404"].response()
        assertEquals("HTTP Not Found Statuscode", 404, response.statusCode().toLong())
        assertEquals("HTTP Not Found Status", "NOT FOUND", response.status().statusMessage())
        response = Http["https://httpbin.org/status/500"].response()
        assertEquals("HTTP Internal Server Error Statuscode", 500, response.statusCode().toLong())
        assertEquals("HTTP Internal Server Error Status", "INTERNAL SERVER ERROR", response.status().statusMessage())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPost() {
        val response = Http[URL("https://httpbin.org/post")].method("POST").response()
        assertEquals("Status", 200, response.status().status().toLong())
        assertNotNull("Response String", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithArguments() {
        val response = Http.post("https://httpbin.org/post").data("param", "2").response()
        assertEquals("Status", 200, response.statusCode().toLong())
        val content = parser!!.parse(response.responseString()).asJsonObject
        val form = content.getAsJsonObject("form")
        val param = form.get("param")
        assertNotNull("Form Param", param)
        assertEquals("Form Param Value", "2", param.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithData() {
        val response = Http.post("https://httpbin.org/post").entity("Value is 2").contentType("text/plain").response()
        assertEquals("Status", 200, response.statusCode().toLong())
        val content = parser!!.parse(response.responseString()).asJsonObject
        val data = content.get("data")
        assertNotNull("Data", data)
        assertEquals("Data Value", "Value is 2", data.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testGetWithCookies() {
        val response = Http[URL("https://httpbin.org/cookies")].cookie("Session", "abcd1234").response()
        val content = parser!!.parse(response.responseString()).asJsonObject
        val cookies = content.getAsJsonObject("cookies")
        val cookie = cookies.get("Session")
        assertNotNull("Cookies", cookie)
        assertEquals("Cookie Value", "abcd1234", cookie.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testBasicAuthentication() {
        val response = Http["https://httpbin.org/basic-auth/test/test"].basicAuth("test", "test").response()
        assertNotNull(response)
        assertEquals("Status", 200, response.statusCode().toLong())
    }

    companion object {

        private var parser: JsonParser? = null

        @BeforeClass
        @Throws(Exception::class)
        fun setUp() {
            parser = JsonParser()
            Http.installDriver(DefaultDriver())
        }
    }
}
