package org.web25.http

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.web25.http.server.Configurator
import org.web25.http.util.handler
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfTest {

    @Test
    fun loadServerFromConf(){
        val http = Http()
        val server = http.server(File(javaClass.classLoader.getResource("server.conf").toURI())).start()
        val response = http.get("http://localhost:8080/").response()
        assertNotNull(server)
        assertNotNull(response)
        assertEquals(404, response.statusCode())
        server.stop()
    }

    @Test
    fun newServerConfigRes() {
        val http = Http()
        val server = http.server(Configurator resource "server.conf").start()
        val response = http.get("http://localhost:8080/").response()
        assertNotNull(server)
        assertNotNull(response)
        assertEquals(404, response.statusCode())
        server.stop()
    }

    @Test
    fun newServerConfigFile() {
        val http = Http()
        val server = http.server(Configurator file "temp/config.properties").start()
        val response = http.get("http://localhost:8080/").response()
        assertNotNull(server)
        assertNotNull(response)
        assertEquals(404, response.statusCode())
        server.stop()
    }

    companion object {

        @BeforeClass
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

        @AfterClass
        @JvmStatic
        fun tearDown() {
            File("temp/config.properties").deleteOnExit()
        }
    }
}