package org.web25.http

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.web25.http.server.HttpServer
import org.web25.http.util.handler
import java.time.ZonedDateTime
import kotlin.test.assertEquals

/**
 * Created by felix on 2/22/17.
 */
internal class HttpCookieServerTest {

    @Test
    fun testSimpleCookie() {
        http.get("http://localhost:$SERVER_PORT/simple/")()
        val res = http.post("http://localhost:$SERVER_PORT/simple/")()
        assertEquals(200, res.statusCode())
    }

    @Test
    fun testExpiringCookie() {
        http.get("http://localhost:$SERVER_PORT/expiringCookie/")()
        val res = http.post("http://localhost:$SERVER_PORT/expiringCookie/")()
        assertEquals(200, res.statusCode())
    }

    @Test
    fun testMaxAgeCookie() {
        http.get("http://localhost:$SERVER_PORT/maxAgeCookie/")()
        val res = http.post("http://localhost:$SERVER_PORT/maxAgeCookie/")()
        assertEquals(200, res.statusCode())
    }

    @Test
    fun testInvalidCookieDomain() {
        http.get("http://localhost:$SERVER_PORT/invalidCookieDomain/")()
        val res = http.post("http://localhost:$SERVER_PORT/invalidCookieDomain/")()
        assertEquals(200, res.statusCode())
    }

    @Test
    fun testPathScopes() {
        http.get("http://localhost:$SERVER_PORT/pathScopes/subpath/")()
        val parentPath = http.post("http://localhost:$SERVER_PORT/pathScopes/")()
        assertEquals(200, parentPath.statusCode())
        val subPath = http.post("http://localhost:$SERVER_PORT/pathScopes/subpath/")()
        assertEquals(200, subPath.statusCode())
        val otherSubPath = http.post("http://localhost:$SERVER_PORT/pathScopes/otherSubpath")()
        assertEquals(200, otherSubPath.statusCode())
    }

    companion object {

        val SERVER_PORT = 8080

        val http = Http()
        val server: HttpServer = http.server(SERVER_PORT)

        @BeforeAll
        @JvmStatic
        internal fun setUp() {
            val simpleCookieRouter = http.router()
                    .get("/", handler { request, response ->
                        response.cookie("hello", "world")
                        true
                    })
                    .post("/", handler { request, response ->
                        if("hello" in request.cookies && request.cookies["hello"].value == "world") {
                            response.status(200)
                        } else {
                            response.status(400)
                            System.err.println("Cookie not found.")
                        }
                        true
                    })
            val expiringCookieRouter = http.router()
                    .get("/", handler { request, response ->
                        val current = ZonedDateTime.now()
                        response.cookie(HttpCookie("correct", "correct", expires = current.plusSeconds(20)))
                        response.cookie(HttpCookie("expired", "expired", expires = current.minusSeconds(10)))
                        true
                    })
                    .post("/", handler { request, response ->
                        if("correct" in request.cookies && "expired" !in request.cookies) {
                            response.status(200)
                        } else {
                            response.status(400)
                            System.err.println("Cookie should have expired.")
                        }
                        true
                    })
            val maxAgeCookieRouter = http.router()
                    .get("/", handler { request, response ->
                        response.cookie(HttpCookie("correct", "correct", maxAge = 3000))
                        response.cookie(HttpCookie("expired", "expired", maxAge = -10))
                        true
                    })
                    .post("/", handler { request, response ->
                        if("correct" in request.cookies && "expired" !in request.cookies) {
                            response.status(200)
                        } else {
                            response.status(400)
                            System.err.println("Cookie should have expired.")
                        }
                        true
                    })
            val invalidCookieDomainRouter = http.router()
                    .get("/", handler { request, response ->
                        response.cookie(HttpCookie("correct", "correct", domain = "localhost"))
                        response.cookie(HttpCookie("incorrect", "incorrect", domain = "google.com"))
                        true
                    })
                    .post("/", handler { request, response ->
                        if("correct" in request.cookies && "incorrect" !in request.cookies) {
                            response.status(200)
                        } else {
                            response.status(400)
                            System.err.println("Cookie for wrong domain should not be present")
                        }
                        true
                    })
            val pathScopesRouter = http.router()
                    .get("/subpath/", handler { request, response ->
                        response.cookie(HttpCookie("visible", "visible", path = "/pathScopes/"))
                        response.cookie("invisible", "invisible")
                        true
                    })
                    .post("/", handler { request, response ->
                        if("visible" in request.cookies && "invisible" !in request.cookies) {
                            response.status(200)
                        } else {
                            response.status(400)
                            System.err.println("Cookie for subpath sould not be visible in parent path")
                        }
                        true
                    })
                    .post("/subpath/", handler { request, response ->
                        if("visible" in request.cookies && "invisible" in request.cookies) {
                            response.status(200)
                        } else {
                            response.status(400)
                            System.err.println("Cookies for subpath should be visible in same subpath")
                        }
                        true
                    })
                    .post("/otherSubpath", handler { request, response ->
                        if("visible" in request.cookies && "invisible" !in request.cookies) {
                            response.status(200)
                        } else {
                            response.status(400)
                            System.err.println("Cookie for subpath should not be visible in different subpath")
                        }
                        true
                    })
            server.use("/simple", simpleCookieRouter)
                    .use("/expiringCookie", expiringCookieRouter)
                    .use("/maxAgeCookie", maxAgeCookieRouter)
                    .use("/invalidCookieDomain", invalidCookieDomainRouter)
                    .use("/pathScopes", pathScopesRouter)
                    .start()
        }

        @AfterAll
        @JvmStatic
        internal fun tearDown() {
            server.stop()
        }
    }
}