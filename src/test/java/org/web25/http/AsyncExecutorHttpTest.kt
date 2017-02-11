package org.web25.http

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test
import org.web25.http.auth.Authentication
import org.web25.http.helper.DefaultHttpContext

/**
 * Created by felix on 2/9/16.
 */
@Ignore
class AsyncExecutorHttpTest {

    val context = DefaultHttpContext()
    val http = Http(context = context, driver = HttpDrivers.asyncDriver(5, context))
    val parser: JsonParser = JsonParser()

    @Test
    @Throws(Exception::class)
    fun testHttpGet() {
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/get").response()
        assertEquals("Status", 200, response.status().status().toLong())
        assertNotNull("Response String", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpGetWithParameters() {
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/get?param=2").response()
        assertEquals("Status", 200, response.status().status().toLong())
        val content = parser.parse(response.responseString()).asJsonObject
        val args = content.getAsJsonObject("args")
        val param = args.get("param")
        assertNotNull("Param", param)
        assertEquals("Param Value", "2", param.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testHttpStatusParsing() {
        var response = http.get("http://" + TestConstants.HTTP.HOST + "/status/200").response()
        assertEquals("HTTP OK Statuscode", 200, response.statusCode().toLong())
        assertEquals("HTTP OK Status", "OK", response.status().statusMessage())
        response = http.get("http://" + TestConstants.HTTP.HOST + "/status/404").response()
        assertEquals("HTTP Not Found Statuscode", 404, response.statusCode().toLong())
        assertEquals("HTTP Not Found Status", "NOT FOUND", response.status().statusMessage())
        response = http.get("http://" + TestConstants.HTTP.HOST + "/status/500").response()
        assertEquals("HTTP Internal Server Error Statuscode", 500, response.statusCode().toLong())
        assertEquals("HTTP Internal Server Error Status", "INTERNAL SERVER ERROR", response.status().statusMessage())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPost() {
        val response = http.post("http://" + TestConstants.HTTP.HOST + "/post").response()
        assertEquals("Status", 200, response.status().status().toLong())
        assertNotNull("Response String", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithArguments() {
        val response = http.post("http://" + TestConstants.HTTP.HOST + "/post").data("param", "2").response()
        assertEquals("Status", 200, response.statusCode().toLong())
        val content = parser.parse(response.responseString()).asJsonObject
        val form = content.getAsJsonObject("form")
        val param = form.get("param")
        assertNotNull("Form Param", param)
        assertEquals("Form Param Value", "2", param.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithData() {
        val response = http.post("http://" + TestConstants.HTTP.HOST + "/post").entity("Value is 2").contentType("text/plain").response()
        assertEquals("Status", 200, response.statusCode().toLong())
        val content = parser.parse(response.responseString()).asJsonObject
        val data = content.get("data")
        assertNotNull("Data", data)
        assertEquals("Data Value", "Value is 2", data.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testGetWithCookies() {
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/cookies").cookie("Session", "abcd1234").response()
        val content = parser.parse(response.responseString()).asJsonObject
        val cookies = content.getAsJsonObject("cookies")
        val cookie = cookies.get("Session")
        assertNotNull("Cookies", cookie)
        assertEquals("Cookie Value", "abcd1234", cookie.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testBasicAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.basic({"test"}, {"test"}))
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/basic-auth/test/test").response()
        assertNotNull(response)
        assertEquals("Status", 200, response.statusCode().toLong())
    }

    @Test
    @Ignore("httpbin does not support digest authentication with the implemented approach")
    @Throws(Exception::class)
    fun testDigestAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.digest({"test"}, {"test"}))
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/digest-auth/auth/test/test").response()
        assertNotNull(response)
        assertEquals("Status", 200, response.statusCode().toLong())
    }

}
