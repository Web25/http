package org.web25.http

import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.web25.http.auth.Authentication

/**
 * Created by felix on 1/19/16.
 */
internal class HttpTest {

    val http = Http()
    val parser = JsonParser()

    @Test
    fun testHttpGet() {
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/get").response()
        assertEquals(200, response.status().status().toLong())
        assertNotNull(response.responseString())
    }

    @Test
    fun testHttpGetWithParameters() {
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/get?param=2").response()
        assertEquals(200, response.status().status().toLong())
        val content = parser.parse(response.responseString()).asJsonObject
        val args = content.getAsJsonObject("args")
        val param = args.get("param")
        assertNotNull(param)
        assertEquals("2", param.asString)
    }

    @Test
    fun testHttpStatusParsing() {
        var response = http.get("http://" + TestConstants.HTTP.HOST + "/status/200").response()
        assertEquals(200, response.statusCode().toLong())
        assertEquals("OK", response.status().statusMessage())
        response = http.get("http://" + TestConstants.HTTP.HOST + "/status/404").response()
        assertEquals(404, response.statusCode().toLong())
        assertEquals("NOT FOUND", response.status().statusMessage())
        response = http.get("http://" + TestConstants.HTTP.HOST + "/status/500").response()
        assertEquals(500, response.statusCode().toLong())
        assertEquals("INTERNAL SERVER ERROR", response.status().statusMessage())
    }

    @Test
    fun testHttpPost() {
        val response = http.post("http://" + TestConstants.HTTP.HOST + "/post").response()
        assertEquals(200, response.status().status().toLong())
        assertNotNull(response.responseString())
    }

    @Test
    fun testHttpPostWithArguments() {
        val request = http.post("http://" + TestConstants.HTTP.HOST + "/post").entity(mapOf(Pair("param", "2")))
        val response = request.response()
        assertEquals(200, response.statusCode().toLong())
        val content = parser.parse(response.responseString()).asJsonObject
        val form = content.getAsJsonObject("form")
        val param = form.get("param")
        assertNotNull(param)
        assertEquals("2", param.asString)
    }

    @Test
    fun testHttpPostWithData() {
        val response = http.post("http://" + TestConstants.HTTP.HOST + "/post").entity("Value is 2").contentType("text/plain").response()
        assertEquals(200, response.statusCode().toLong())
        val content = parser.parse(response.responseString()).asJsonObject
        val data = content.get("data")
        assertNotNull(data)
        assertEquals("Value is 2", data.asString)
    }

    @Test
    fun testGetWithCookies() {
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/cookies").cookie("Session", "abcd1234").response()
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
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/basic-auth/test/test").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
    }

    @Test
    @Disabled("httpbin does not support digest authentication with the implemented approach")
    fun testDigestAuthentication() {
        val http = Http()
        http.context.addAuthentication(Authentication.digest({"test"}, {"test"}))
        val response = http.get("http://" + TestConstants.HTTP.HOST + "/digest-auth/auth/test/test").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
    }

    @Test
    fun testQueryParsing(){
        val http = Http()
        val request = http.get("http://${TestConstants.HTTP.HOST}/get")
        request.query["param"] = 2
        val response = request.response()
        assertEquals(200, response.status().status().toLong())
        val content = parser.parse(response.responseString()).asJsonObject
        val args = content.getAsJsonObject("args")
        val param = args.get("param")
        assertNotNull(param)
        assertEquals("2", param.asString)
    }

    @Test
    fun testDynamicPathVariables(){
        val http = Http()
        var request = http.get("http://${TestConstants.HTTP.HOST}/status/{code}")
        request.path["code"] = 200
        var response = request.response()
        assertEquals(200, response.statusCode())

        request = http.get("http://${TestConstants.HTTP.HOST}/status/{code}")
        request.path["code"] = 404
        response = request.response()
        assertEquals(404, response.statusCode())
    }

    @Test()
    fun testNPEOnDynamicPathVars(){
        val http = Http()
        val request = http.get("http://${TestConstants.HTTP.HOST}/status/{code}")
        assertThrows<NullPointerException>(NullPointerException::class.java, {request.response()})
    }

    /*@Test
    @Throws(Exception::class)
    fun testEvents() {
        http.get("http://" + TestConstants.HTTP.HOST + "/get").event(HttpEventType.ALL, object : HttpEventHandler {
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
    }*/
}