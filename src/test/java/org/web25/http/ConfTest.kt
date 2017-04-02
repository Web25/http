package org.web25.http

import com.jayway.awaitility.Awaitility
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.web25.http.server.Configurator
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ConfTest {

    @Test
    fun loadServerFromConf(){
        val http = Http()
        val server = http.server(File(javaClass.classLoader.getResource("server.conf").toURI())).start()
        Awaitility.await().until<Boolean> { server.ready() }
        val response = http.get("http://localhost:3000/").response()
        assertNotNull(server)
        assertNotNull(response)
        assertEquals(404, response.statusCode())
        server.stop()
    }

    @Test
    fun newServerConfigRes() {
        val http = Http()
        val server = http.server(Configurator resource "server.conf").start()
        Awaitility.await().until<Boolean> { server.ready() }
        val response = http.get("http://localhost:3000/").response()
        assertNotNull(server)
        assertNotNull(response)
        assertEquals(404, response.statusCode())
        server.stop()
    }

    @Test
    fun newServerConfigFile() {
        val http = Http()
        val server = http.server(Configurator file "temp/config.properties").start()
        Awaitility.await().until<Boolean> { server.ready() }
        val response = http.get("http://localhost:4000/").response()
        assertNotNull(server)
        assertNotNull(response)
        assertEquals(404, response.statusCode())
        server.stop()
    }

    companion object {

        @BeforeAll
        @JvmStatic
        fun setUp() {
            val temp = File("temp")
            if(!temp.exists()) temp.mkdir()
            val out = PrintStream("temp/config.properties")
            out.print("""port=4000
ssl=false
""")
            out.close()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            File("temp/config.properties").deleteOnExit()
        }
    }
}