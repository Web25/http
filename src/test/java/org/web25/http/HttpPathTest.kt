package org.web25.http

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Created by felix on 3/6/17.
 */
internal class HttpPathTest {

    @Test
    internal fun testPathCreation() {
        val path = HttpPath("/{action}/{userId}")
        assertThrows<KotlinNullPointerException>(KotlinNullPointerException::class.java, {
            path.buildActualPath()
        })
        path["action"] = "show"
        path["userId"] = "12"
        assertEquals("/show/12", path.buildActualPath())
    }

    @Test
    internal fun testReadValues() {
        val path = HttpPath("/{action}/{userId}")
        assertTrue(path.matches("/show/12"))
        assertFalse(path.matches("/show/"))
        assertFalse(path.matches("/show/12/abacus"))
        val incomingPath = path.match("/show/12")
        assertEquals("show", incomingPath["action"])
        assertEquals("12", incomingPath["userId"])
    }

    @Test
    fun testHierarchicalPaths() {
        val basePath = HttpPath("/rest/api/")
        val userPath = HttpPath("/users/")
        val clientPath = HttpPath("/clients/")
        userPath.prependPath(basePath)
        clientPath.prependPath(basePath)
        assertFalse(userPath.matches("/rest/api/users"))
        assertTrue(userPath.matches("/rest/api/users/"))
        assertFalse(clientPath.matches("/rest/api/clients"))
        assertTrue(clientPath.matches("/rest/api/clients/"))
        val userActionPath = HttpPath("/{userId}/{action}/")
        userActionPath.prependPath(userPath)
        assertTrue(userActionPath.matches("/rest/api/users/12/show/"))
        assertFalse(userActionPath.matches("/rest/api/users/12/show"))
        val reqPath = userActionPath.match("/rest/api/users/12/show/")
        assertEquals("show", reqPath["action"])
        assertEquals("12", reqPath["userId"])
    }
}