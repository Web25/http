package org.web25.http.entities

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.web25.http.Http
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * Created by felix on 3/13/17.
 */
class FileEntityTest {

    @Test
    fun testTextFile() {
        val fileEntity = FileEntity(File("test/test.txt"), http.context)
        assertEquals("Hello World\n", String(fileEntity.getBytes()))
        assertEquals("text/plain", fileEntity.contentType)
    }

    @Test
    fun testHTMLFile() {
        val fileEntity = FileEntity(File("test/test.html"), http.context)
        assertEquals("<!DOCTYPE html><html><head><title>Hello</title></head><body>Hello</body></html>\n", String(fileEntity.getBytes()))
        assertEquals("text/html", fileEntity.contentType)
    }

    @Test
    fun testPng() {
        val fileEntity = FileEntity(File("test/test.png"), http.context)
        assertEquals("image/png", fileEntity.contentType)
    }

    @Test
    fun testGif() {
        val fileEntity = FileEntity(File("test/test.gif"), http.context)
        assertEquals("image/gif", fileEntity.contentType)
    }

    companion object {

        val http = Http()

        @JvmStatic
        @BeforeAll
        fun loadFiles() {
            val test = File("test")
            if (!test.exists()) {
                test.mkdir()
            }
            val printStream = PrintStream("test/test.html")
            printStream.println("<!DOCTYPE html><html><head><title>Hello</title></head><body>Hello</body></html>")
            printStream.close()
            val textPrintStream = PrintStream("test/test.txt")
            textPrintStream.println("Hello World")
            textPrintStream.close()
            http.get("http://www.schaik.com/pngsuite/basn6a08.png").pipe(FileOutputStream("test/test.png")).execute()
            http.get("http://www.schaik.com/pngsuite/basn6a08.gif").pipe(FileOutputStream("test/test.gif")).execute()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            FileUtils.deleteDirectory(File("test"))
        }
    }
}