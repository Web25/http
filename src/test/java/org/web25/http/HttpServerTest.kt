package org.web25.http

import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import org.web25.http.auth.Authentication
import org.web25.http.drivers.DefaultDriver
import org.web25.http.handlers.DirectoryFileHandler
import org.web25.http.handlers.FileHandler
import org.web25.http.handlers.auth.CredentialProvider
import org.web25.http.server.HttpHandler
import org.web25.http.server.HttpMiddleware
import org.web25.http.server.HttpServer
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Created by felix on 4/25/16.
 */
class HttpServerTest {

    @Rule
    var timeout: TestRule = DisableOnDebug(Timeout(10, SECONDS))

    @Test
    @Throws(Exception::class)
    fun testGet() {
        val response = Http["http://localhost:8080/"].response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("7", response.header("Content-Length")!!.value)
        assertEquals("Did it!", response.responseString())
        assertEquals("text/plain", response.header("Content-Type")!!.value)
    }

    @Test
    @Throws(Exception::class)
    fun testPost() {
        val response = Http.post("http://localhost:8080/").entity("This is test!").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("This is test!", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testStyle() {
        val response = Http["http://localhost:8080/style/default"].response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("text/css", response.header("Content-Type")!!.value)
        assertEquals("body, html {font-family: \"Arial\";}", response.responseString())
    }

    @Test
    @Throws(Exception::class)
    fun testResource() {
        val response = Http["http://localhost:8080/test-res"].response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("Hello World", response.responseString())
        assertEquals("text/plain", response.header("Content-Type")!!.value)
    }

    @Test
    @Throws(Exception::class)
    fun testFile() {
        val response = Http["http://localhost:8080/test-file"].response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("Hello World", response.responseString())
        assertEquals("text/plain", response.header("Content-Type")!!.value)
    }

    @Test
    @Throws(Exception::class)
    fun testDir() {
        val response = Http["http://localhost:8080/test-dir/test.txt"].response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("Hello World", response.responseString())
        assertEquals("text/plain", response.header("Content-Type")!!.value)
    }


    @Test
    @Throws(Exception::class)
    fun testBasicAuth() {
        var response = Http["http://localhost:8080/auth/basic/"].basicAuth("felix", "test").response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("7", response.header("Content-Length")!!.value)
        assertEquals("Did it!", response.responseString())
        assertEquals("text/plain", response.header("Content-Type")!!.value)
        response = Http["http://localhost:8080/auth/basic/"].response()
        assertNotNull(response)
        assertEquals(401, response.statusCode().toLong())
        assertTrue(response.hasHeader("WWW-Authenticate"))
        assertTrue(response.header("WWW-Authenticate")!!.value.startsWith("Basic"))
    }

    @Test
    @Throws(Exception::class)
    fun testDigestAuth() {
        var response = Http["http://localhost:8080/auth/digest/"].using(Authentication.digest("felix", "test")).response()
        assertNotNull(response)
        assertEquals(200, response.statusCode().toLong())
        assertEquals("7", response.header("Content-Length")!!.value)
        assertEquals("Did it!", response.responseString())
        assertEquals("text/plain", response.header("Content-Type")!!.value)
        response = Http["http://localhost:8080/auth/digest/"].response()
        assertNotNull(response)
        assertEquals(401, response.statusCode().toLong())
        assertTrue(response.hasHeader("WWW-Authenticate"))
        assertTrue(response.header("WWW-Authenticate")!!.value.startsWith("Digest"))
    }

    companion object {

        private var httpServer: HttpServer? = null

        @BeforeClass
        @Throws(Exception::class)
        fun setUp() {
            Http.installDriver(DefaultDriver())
            val router = Http.router()
                    .get("/default", { req:HttpRequest, res:HttpResponse ->
                        res.entity("body, html {font-family: \"Arial\";}")
                        res.header("Content-Type", "text/css")
                        true
                    } as HttpHandler)
            val authRouter = Http.router()
                    .use("/basic", Http.router()
                            .use(org.web25.http.handlers.Authentication.basic("Basic_Authentication_Realm", { username: String -> CredentialProvider.Credentials("felix", "test") } as CredentialProvider))
                            .get("/", { request: HttpRequest, response: HttpResponse ->
                                response.entity("Did it!")
                                true
                            } as HttpHandler)
                    ).use("/digest", Http.router()
                    .use(org.web25.http.handlers.Authentication.digest("Digest_Authentication_Realm", { username: String -> CredentialProvider.Credentials("felix", "test") } as CredentialProvider))
                    .get("/", { request: HttpRequest, response: HttpResponse ->
                        response.entity("Did it!")
                        true
                    } as HttpHandler)
            )
            httpServer = Http.server(8080)
                    .get("/", { request: HttpRequest, response: HttpResponse ->
                        response.entity("Did it!")
                        true
                    } as HttpHandler)
                    .post("/", { request: HttpRequest, response: HttpResponse ->
                        response.entity(request.entityBytes())
                        true
                    } as HttpHandler)
                    .get("/test-res", FileHandler.Companion.resource("/test.txt", true, "text/plain"))
                    .get("/test-file", FileHandler.Companion.buffered(File("test.txt"), true, "text/plain"))
                    .use("/test-dir", DirectoryFileHandler(File("testDocs"), false, 0))
                    .use("/style", router)
                    .use("/auth", authRouter)
                    .after({ request: HttpRequest, response: HttpResponse ->
                        System.out.printf("%-10s %s - %s byte(s)\n", request.method(), request.path(),
                                if (response.hasHeader("Content-Length")) response.header("Content-Length")!!.value else " --")
                    } as HttpMiddleware).start()
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
