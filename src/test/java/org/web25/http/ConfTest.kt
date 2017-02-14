package org.web25.http

import org.junit.Test
import org.web25.http.util.handler
import java.io.File
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
    }

}