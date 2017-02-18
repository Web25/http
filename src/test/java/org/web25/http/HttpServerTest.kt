package org.web25.http

import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.web25.http.auth.Authentication
import org.web25.http.handlers.DirectoryFileHandler
import org.web25.http.handlers.FileHandler
import org.web25.http.handlers.auth.CredentialProvider
import org.web25.http.server.HttpServer
import org.web25.http.util.handler
import org.web25.http.util.middleware
import java.io.File
import java.io.PrintStream
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Created by felix on 4/25/16.
 */
class HttpServerTest {
    
    val http = Http()

    @Test
    @Throws(Exception::class)
    fun testGet() {
        val response = http.get("http://localhost:8080/").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("7", response.header("Content-Length").value)
        assertEquals("Did it!", response.responseString())
        assertEquals("text/plain", response.header("Content-Type").value)
    }

    @Test
    @Throws(Exception::class)
    fun testPost() {
        val response = http.post("http://localhost:8080/").entity("This is test!").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("This is test!", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testStyle() {
        val response = http.get("http://localhost:8080/style/default").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("text/css", response.header("Content-Type").value)
        assertEquals("body, html {font-family: \"Arial\";}", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testResource() {
        val response = http.get("http://localhost:8080/test-res").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("Hello World", response.responseString())
        assertEquals("text/plain", response.header("Content-Type").value)
    }

    @Test
    @Throws(Exception::class)
    fun testFile() {
        val response = http.get("http://localhost:8080/test-file").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("Hello World", response.responseString())
        assertEquals("text/plain", response.header("Content-Type").value)
    }

    @Test
    @Throws(Exception::class)
    fun testDir() {
        val response = http.get("http://localhost:8080/test-dir/test.txt").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("Hello World", response.responseString())
        assertEquals("text/plain", response.header("Content-Type").value)
    }


    @Test
    @Throws(Exception::class)
    fun testBasicAuth() {
        val http = Http()
        var response = http.get("http://localhost:8080/auth/basic/").response()
        assertNotNull(response)
        assertEquals(401, response.statusCode().toLong())
        assertTrue(response.hasHeader("WWW-Authenticate"))
        assertTrue(response.header("WWW-Authenticate").value.startsWith("Basic"))
        http.context.addAuthentication(Authentication.basic("felix", "test"))
        response = http.get("http://localhost:8080/auth/basic/").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("7", response.header("Content-Length").value)
        assertEquals("Did it!", response.responseString())
        assertEquals("text/plain", response.header("Content-Type").value)
    }

    @Test
    @Throws(Exception::class)
    fun testDigestAuth() {
        val http = Http()
        var response = http.get("http://localhost:8080/auth/digest/").response()
        assertNotNull(response)
        assertEquals(401, response.statusCode().toLong())
        assertTrue(response.hasHeader("WWW-Authenticate"))
        assertTrue(response.header("WWW-Authenticate").value.startsWith("Digest"))
        http.context.addAuthentication(Authentication.digest("felix", "test"))
        response = http.get("http://localhost:8080/auth/digest/").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("7", response.header("Content-Length").value)
        assertEquals("Did it!", response.responseString())
        assertEquals("text/plain", response.header("Content-Type").value)
    }

    @Test
    @Throws(Exception::class)
    fun testDynamicPath(){
        val http = Http()
        val response = http.get("http://localhost:8080/dynamic/value1").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode())
        assertEquals("Yay", response.responseString())
    }

    @Test
    fun testCookies() {
        val http = Http()
        val response = http.get("http://localhost:8080/cookie/").response()
        assertEquals(200, response.statusCode())
        assertEquals("en-US", response.cookie("lang").value)
    }

    companion object {

        private var httpServer: HttpServer? = null

        private val credentialProvider = object : CredentialProvider {
            override fun findByUsername(username: String): CredentialProvider.Credentials {
                if (username == "felix") {
                    return CredentialProvider.Credentials("felix", "test")
                } else {
                    throw RuntimeException("No Credentials found!")
                }
            }
        }

        @BeforeClass
        @Throws(Exception::class)
        @JvmStatic
        fun setUp() {
            val http = Http()
            val router = http.router()
                    .get("/default", handler { req, res ->
                        res.entity("body, html {font-family: \"Arial\";}")
                        res.header("Content-Type", "text/css")
                        true
                    })
            val authRouter = http.router()
                    .use("/basic", http.router()
                            .use(org.web25.http.handlers.Authentication.basic("Basic_Authentication_Realm", credentialProvider))
                            .get("/", handler { request, response ->
                                response.entity("Did it!")
                                true
                            })
                    ).use("/digest", http.router()
                    .use(org.web25.http.handlers.Authentication.digest("Digest_Authentication_Realm", credentialProvider))
                    .get("/", handler { request, response ->
                        response.entity("Did it!")
                        true
                    })
            )
            val cookieRouter = http.router()
                    .get("/", handler { request, response ->
                        response.cookie(HttpCookie("lang", "en-US", expires = ZonedDateTime.of(2021, 6, 9, 10, 18, 14, 0, ZoneId.of("GMT")), path = "/cookie/", domain = "localhost", secure = true,
                                httpOnly = true))
                        true
                    })
            httpServer = http.server(8080)
                    .get("/", handler { request, response ->
                        response.entity("Did it!")
                        true
                    })
                    .post("/", handler { request, response ->
                        response.entity(request.entityBytes())
                        true
                    })
                    .get("/test-res", FileHandler.Companion.resource("/test.txt", true, "text/plain"))
                    .get("/test-file", FileHandler.Companion.buffered(File("test.txt"), true, "text/plain"))
                    .get("/dynamic/{path}", handler { request, response ->
                        response.entity("Yay")
                        true
                    })
                    .use("/test-dir", DirectoryFileHandler(File("testDocs"), false, 0))
                    .use("/style", router)
                    .use("/auth", authRouter)
                    .use("/cookie", cookieRouter)
                    .after(middleware { request, response ->
                        System.out.printf("%-10s %s - %s byte(s)\n", request.method(), request.path(),
                                if (response.hasHeader("Content-Length")) response.header("Content-Length").value else " --")
                    }).start()
            var printStream = PrintStream("test.txt")
            printStream.print("Hello World")
            printStream.close()
            val testDocs = File("testDocs")
            if (!testDocs.exists()) {
                testDocs.mkdir()
            }
            printStream = PrintStream("testDocs/test.txt")
            printStream.print("Hello World")
            printStream.close()
            while (!httpServer!!.ready())
                try {
                    Thread.sleep(20)
                } catch (ignored: InterruptedException) {

                }

        }

        @AfterClass
        @Throws(Exception::class)
        fun tearDown() {
            httpServer!!.stop()
            File("test.txt").deleteOnExit()
            FileUtils.deleteDirectory(File("testDocs"))
        }
    }
}
