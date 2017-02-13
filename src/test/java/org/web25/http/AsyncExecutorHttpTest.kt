package org.web25.http

import com.google.gson.JsonParser
import com.jayway.awaitility.Awaitility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test
import org.web25.http.auth.Authentication
import org.web25.http.helper.DefaultHttpContext

/**
 * Created by felix on 2/9/16.
 */
class AsyncExecutorHttpTest {

    val context = DefaultHttpContext()
    val http = Http(context = context, driver = HttpDrivers.asyncDriver(5, context))
    val parser: JsonParser = JsonParser()

    @Test
    @Throws(Exception::class)
    fun testHttpGet() {
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/get").execute {
            assertEquals("Status", 200, it.status().status().toLong())
            assertNotNull("Response String", it.responseString())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Throws(Exception::class)
    fun testHttpGetWithParameters() {
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/get?param=2").execute {
            assertEquals("Status", 200, it.status().status().toLong())
            val content = parser.parse(it.responseString()).asJsonObject
            val args = content.getAsJsonObject("args")
            val param = args.get("param")
            assertNotNull("Param", param)
            assertEquals("Param Value", "2", param.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Throws(Exception::class)
    fun testHttpStatusParsing() {
        var fin1 = false
        var fin2 = false
        var fin3 = false
        http.get("http://" + TestConstants.HTTP.HOST + "/status/200").execute {
            assertEquals("HTTP OK Statuscode", 200, it.statusCode().toLong())
            assertEquals("HTTP OK Status", "OK", it.status().statusMessage())
            fin1 = true
        }
        http.get("http://" + TestConstants.HTTP.HOST + "/status/404").execute {
            assertEquals("HTTP Not Found Statuscode", 404, it.statusCode().toLong())
            assertEquals("HTTP Not Found Status", "NOT FOUND", it.status().statusMessage())
            fin2 = true
        }
        http.get("http://" + TestConstants.HTTP.HOST + "/status/500").execute {
            assertEquals("HTTP Internal Server Error Statuscode", 500, it.statusCode().toLong())
            assertEquals("HTTP Internal Server Error Status", "INTERNAL SERVER ERROR", it.status().statusMessage())
            fin3 = true
        }
        Awaitility.await().until<Boolean> { fin1 && fin2 && fin3 }
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPost() {
        var finished = false
        http.post("http://" + TestConstants.HTTP.HOST + "/post").execute {
            assertEquals("Status", 200, it.status().status().toLong())
            assertNotNull("Response String", it.responseString())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithArguments() {
        var finished = false
        http.post("http://" + TestConstants.HTTP.HOST + "/post").data("param", "2").execute {
            assertEquals("Status", 200, it.statusCode().toLong())
            val content = parser.parse(it.responseString()).asJsonObject
            val form = content.getAsJsonObject("form")
            val param = form.get("param")
            assertNotNull("Form Param", param)
            assertEquals("Form Param Value", "2", param.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithData() {
        var finished = false
        http.post("http://" + TestConstants.HTTP.HOST + "/post").entity("Value is 2").contentType("text/plain").execute {
            assertEquals("Status", 200, it.statusCode().toLong())
            val content = parser.parse(it.responseString()).asJsonObject
            val data = content.get("data")
            assertNotNull("Data", data)
            assertEquals("Data Value", "Value is 2", data.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Throws(Exception::class)
    fun testGetWithCookies() {
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/cookies").cookie("Session", "abcd1234").execute {
            val content = parser.parse(it.responseString()).asJsonObject
            val cookies = content.getAsJsonObject("cookies")
            val cookie = cookies.get("Session")
            assertNotNull("Cookies", cookie)
            assertEquals("Cookie Value", "abcd1234", cookie.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Throws(Exception::class)
    fun testBasicAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.basic({"test"}, {"test"}))
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/basic-auth/test/test").execute {
            assertNotNull(it)
            assertEquals("Status", 200, it.statusCode().toLong())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Ignore("httpbin does not support digest authentication with the implemented approach")
    @Throws(Exception::class)
    fun testDigestAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.digest({"test"}, {"test"}))
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/digest-auth/auth/test/test").execute {
            assertNotNull(it)
            assertEquals("Status", 200, it.statusCode().toLong())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

}
