package org.web25.http

import com.google.gson.JsonParser
import com.jayway.awaitility.Awaitility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.web25.http.auth.Authentication
import org.web25.http.helper.DefaultHttpContext

/**
 * Created by felix on 2/9/16.
 */
internal class AsyncExecutorHttpTest {

    val context = DefaultHttpContext()
    val http = Http(context = context, driver = HttpDrivers.asyncDriver(5, context))
    val parser: JsonParser = JsonParser()

    @Test
    fun testHttpGet() {
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/get").execute {
            assertEquals(200, it.status().status().toLong())
            assertNotNull(it.responseString())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    fun testHttpGetWithParameters() {
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/get?param=2").execute {
            assertEquals(200, it.status().status().toLong())
            val content = parser.parse(it.responseString()).asJsonObject
            val args = content.getAsJsonObject("args")
            val param = args.get("param")
            assertNotNull(param)
            assertEquals("2", param.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    fun testHttpStatusParsing() {
        var fin1 = false
        var fin2 = false
        var fin3 = false
        http.get("http://" + TestConstants.HTTP.HOST + "/status/200").execute {
            assertEquals(200, it.statusCode().toLong())
            assertEquals("OK", it.status().statusMessage())
            fin1 = true
        }
        http.get("http://" + TestConstants.HTTP.HOST + "/status/404").execute {
            assertEquals(404, it.statusCode().toLong())
            assertEquals("NOT FOUND", it.status().statusMessage())
            fin2 = true
        }
        http.get("http://" + TestConstants.HTTP.HOST + "/status/500").execute {
            assertEquals(500, it.statusCode().toLong())
            assertEquals("INTERNAL SERVER ERROR", it.status().statusMessage())
            fin3 = true
        }
        Awaitility.await().until<Boolean> { fin1 && fin2 && fin3 }
    }

    @Test
    fun testHttpPost() {
        var finished = false
        http.post("http://" + TestConstants.HTTP.HOST + "/post").execute {
            assertEquals(200, it.status().status().toLong())
            assertNotNull(it.responseString())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    fun testHttpPostWithArguments() {
        var finished = false
        http.post("http://" + TestConstants.HTTP.HOST + "/post").data("param", "2").execute {
            assertEquals(200, it.statusCode().toLong())
            val content = parser.parse(it.responseString()).asJsonObject
            val form = content.getAsJsonObject("form")
            val param = form.get("param")
            assertNotNull(param)
            assertEquals("2", param.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    fun testHttpPostWithData() {
        var finished = false
        http.post("http://" + TestConstants.HTTP.HOST + "/post").entity("Value is 2").contentType("text/plain").execute {
            assertEquals(200, it.statusCode().toLong())
            val content = parser.parse(it.responseString()).asJsonObject
            val data = content.get("data")
            assertNotNull(data)
            assertEquals("Value is 2", data.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    fun testGetWithCookies() {
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/cookies").cookie("Session", "abcd1234").execute {
            val content = parser.parse(it.responseString()).asJsonObject
            val cookies = content.getAsJsonObject("cookies")
            val cookie = cookies.get("Session")
            assertNotNull(cookie)
            assertEquals("abcd1234", cookie.asString)
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    fun testBasicAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.basic({"test"}, {"test"}))
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/basic-auth/test/test").execute {
            assertNotNull(it)
            assertEquals(200, it.statusCode().toLong())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

    @Test
    @Disabled("httpbin does not support digest authentication with the implemented approach")
    fun testDigestAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.digest({"test"}, {"test"}))
        var finished = false
        http.get("http://" + TestConstants.HTTP.HOST + "/digest-auth/auth/test/test").execute {
            assertNotNull(it)
            assertEquals(200, it.statusCode().toLong())
            finished = true
        }
        Awaitility.await().until<Boolean> { finished }
    }

}
