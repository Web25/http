package org.web25.http

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import org.web25.http.drivers.DefaultDriver
import org.web25.http.events.*
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Created by felix on 1/19/16.
 */
class HttpTest {

    @Rule
    var timeout: TestRule = DisableOnDebug(Timeout(20, TimeUnit.SECONDS))

    @Test
    @Throws(Exception::class)
    fun testHttpGet() {
        val response = Http[URL("http://" + TestConstants.HTTP.HOST + "/get")].response()
        assertEquals("Status", 200, response.status().status().toLong())
        assertNotNull("Response String", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpGetWithParameters() {
        val response = Http[URL("http://" + TestConstants.HTTP.HOST + "/get?param=2")].response()
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
        var response = Http["http://" + TestConstants.HTTP.HOST + "/status/200"].response()
        assertEquals("HTTP OK Statuscode", 200, response.statusCode().toLong())
        assertEquals("HTTP OK Status", "OK", response.status().statusMessage())
        response = Http["http://" + TestConstants.HTTP.HOST + "/status/404"].response()
        assertEquals("HTTP Not Found Statuscode", 404, response.statusCode().toLong())
        assertEquals("HTTP Not Found Status", "NOT FOUND", response.status().statusMessage())
        response = Http["http://" + TestConstants.HTTP.HOST + "/status/500"].response()
        assertEquals("HTTP Internal Server Error Statuscode", 500, response.statusCode().toLong())
        assertEquals("HTTP Internal Server Error Status", "INTERNAL SERVER ERROR", response.status().statusMessage())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPost() {
        val response = Http[URL("http://" + TestConstants.HTTP.HOST + "/post")].method("POST").response()
        assertEquals("Status", 200, response.status().status().toLong())
        assertNotNull("Response String", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithArguments() {
        val response = Http.post("http://" + TestConstants.HTTP.HOST + "/post").data("param", "2").response()
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
        val response = Http.post("http://" + TestConstants.HTTP.HOST + "/post").entity("Value is 2").contentType("text/plain").response()
        assertEquals("Status", 200, response.statusCode().toLong())
        val content = parser!!.parse(response.responseString()).asJsonObject
        val data = content.get("data")
        assertNotNull("Data", data)
        assertEquals("Data Value", "Value is 2", data.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testGetWithCookies() {
        val response = Http[URL("http://" + TestConstants.HTTP.HOST + "/cookies")].cookie("Session", "abcd1234").response()
        val content = parser!!.parse(response.responseString()).asJsonObject
        val cookies = content.getAsJsonObject("cookies")
        val cookie = cookies.get("Session")
        assertNotNull("Cookies", cookie)
        assertEquals("Cookie Value", "abcd1234", cookie.asString)
    }

    @Test
    @Throws(Exception::class)
    fun testBasicAuthentication() {
        val response = Http["http://" + TestConstants.HTTP.HOST + "/basic-auth/test/test"].basicAuth("test", "test").response()
        assertNotNull(response)
        assertEquals("Status", 200, response.statusCode().toLong())
    }

    @Test
    @Ignore("httpbin does not support digest authentication with the implemented approach")
    @Throws(Exception::class)
    fun testDigestAuthentication() {
        val response = Http["http://" + TestConstants.HTTP.HOST + "/digest-auth/auth/test/test"].using(Authentication.digest("test", "test")).response()
        assertNotNull(response)
        assertEquals("Status", 200, response.statusCode().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testEvents() {
        Http["http://" + TestConstants.HTTP.HOST + "/get"].event(HttpEventType.ALL, object : HttpEventHandler {
            override fun handle(event: HttpEvent) {
                if (event.eventType() === HttpEventType.SENT) {
                    val sentEvent = event as HttpSentEvent
                    println(sentEvent.request().requestLine())
                } else if (event.eventType() === HttpEventType.RECEIVED) {
                    val receivedEvent = event as HttpReceivedEvent
                    println(receivedEvent.request().requestLine() + " - " + receivedEvent.response().statusLine())
                } else if (event.eventType() === HttpEventType.HANDLED) {
                    val handledEvent = event as HttpHandledEvent
                    println(handledEvent.request().requestLine() + " - " + handledEvent.response().statusLine() + " - " + if (handledEvent.callbackExecuted()) "HANDLED" else "NOT HANDLED")
                } else {
                    println(event.eventType())
                }
            }
        }).execute()
    }

    companion object {

        private var parser: JsonParser? = null

        @BeforeClass
        @Throws(Exception::class)
        fun setUp() {
            parser = JsonParser()
            Http.installDriver(DefaultDriver())
            println("Using " + TestConstants.HTTP.HOST)
        }
    }
}