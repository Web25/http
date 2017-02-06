package org.web25.http

import com.google.gson.JsonParser
import org.jodah.concurrentunit.Waiter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import org.web25.http.drivers.AsynchronousDriver
import java.net.URL

/**
 * Created by felix on 2/9/16.
 */
class AsyncExecutorHttpTest {

    @Test
    @Throws(Exception::class)
    fun testHttpGet() {
        val waiter = Waiter()
        Http[URL("http://" + TestConstants.HTTP.HOST + "/get")].execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("Status", 200, response.status().status().toLong())
                assertNotNull("Response String", response.responseString())
                waiter.resume()
            }
        })
        try {
            waiter.await(2000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    @Test
    @Throws(Exception::class)
    fun testHttpGetWithParameters() {
        val waiter = Waiter()
        Http[URL("http://" + TestConstants.HTTP.HOST + "/get?param=2")].execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("Status", 200, response.status().status().toLong())
                val content = parser!!.parse(response.responseString()).asJsonObject
                val args = content.getAsJsonObject("args")
                val param = args.get("param")
                assertNotNull("Param", param)
                assertEquals("Param Value", "2", param.asString)
                waiter.resume()
            }
        })
        try {
            waiter.await(3000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    @Test
    @Throws(Exception::class)
    fun testHttpStatusParsing() {
        val waiter1 = Waiter()
        Http["http://" + TestConstants.HTTP.HOST + "/status/200"].execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("HTTP OK Statuscode", 200, response.statusCode().toLong())
                assertEquals("HTTP OK Status", "OK", response.status().statusMessage())
                waiter1.resume()
            }
        })
        val waiter2 = Waiter()
        Http["http://" + TestConstants.HTTP.HOST + "/status/404"].execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("HTTP Not Found Statuscode", 404, response.statusCode().toLong())
                assertEquals("HTTP Not Found Status", "NOT FOUND", response.status().statusMessage())
                waiter2.resume()
            }
        })
        val waiter3 = Waiter()
        Http["http://" + TestConstants.HTTP.HOST + "/status/500"].execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("HTTP Internal Server Error Statuscode", 500, response.statusCode().toLong())
                assertEquals("HTTP Internal Server Error Status", "INTERNAL SERVER ERROR", response.status().statusMessage())
                waiter3.resume()
            }
        })
        try {
            waiter1.await(3000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        try {
            waiter2.await(3000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        try {
            waiter3.await(3000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    @Test
    @Throws(Exception::class)
    fun testHttpPost() {
        val waiter = Waiter()
        Http[URL("http://" + TestConstants.HTTP.HOST + "/post")].method("POST").execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("Status", 200, response.status().status().toLong())
                assertNotNull("Response String", response.responseString())
                waiter.resume()
            }
        })
        try {
            waiter.await(2000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithArguments() {
        val waiter = Waiter()
        Http.post("http://" + TestConstants.HTTP.HOST + "/post").data("param", "2").execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("Status", 200, response.statusCode().toLong())
                val content = parser!!.parse(response.responseString()).asJsonObject
                val form = content.getAsJsonObject("form")
                val param = form.get("param")
                assertNotNull("Form Param", param)
                assertEquals("Form Param Value", "2", param.asString)
                waiter.resume()
            }
        })
        try {
            waiter.await(2000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    @Test
    @Throws(Exception::class)
    fun testHttpPostWithData() {
        val waiter = Waiter()
        Http.post("http://" + TestConstants.HTTP.HOST + "/post").entity("Value is 2").contentType("text/plain").execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertEquals("Status", 200, response.statusCode().toLong())
                val content = parser!!.parse(response.responseString()).asJsonObject
                val data = content.get("data")
                assertNotNull("Data", data)
                assertEquals("Data Value", "Value is 2", data.asString)
                waiter.resume()
            }
        })
        try {
            waiter.await(2000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    @Test
    @Throws(Exception::class)
    fun testGetWithCookies() {
        val waiter = Waiter()
        Http[URL("http://" + TestConstants.HTTP.HOST + "/cookies")].cookie("Session", "abcd1234").execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                val content = parser!!.parse(response.responseString()).asJsonObject
                val cookies = content.getAsJsonObject("cookies")
                val cookie = cookies.get("Session")
                assertNotNull("Cookies", cookie)
                assertEquals("Cookie Value", "abcd1234", cookie.asString)
                waiter.resume()
            }
        })
        try {
            waiter.await(2000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    @Test
    @Throws(Exception::class)
    fun testBasicAuthentication() {
        val waiter = Waiter()
        Http["http://" + TestConstants.HTTP.HOST + "/basic-auth/test/test"].basicAuth("test", "test").execute(object : HttpResponseCallback {
            override fun receivedResponse(response: HttpResponse) {
                assertNotNull(response)
                assertEquals("Status", 200, response.statusCode().toLong())
                waiter.resume()
            }
        })
        try {
            waiter.await(2000)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }

    companion object {

        private var parser: JsonParser? = null

        @BeforeClass
        @Throws(Exception::class)
        fun setUp() {
            parser = JsonParser()
            Http.installDriver(AsynchronousDriver(5))
        }
    }
}
