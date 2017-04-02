package org.web25.http.drivers

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.web25.http.Http
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * Created by felix on 6/28/16.
 */
class DefaultMimeServiceTest {

    @Test
    fun testMimes() {
        val html = File("test/test.html")
        val png = File("test/test.png")
        val gif = File("test/test.gif")
        val gifWrong = File("test/test_wrong.gif")
        val mimeService = DefaultMimeService()
        assertEquals("text/html", mimeService.contentType(html))
        assertEquals("image/png", mimeService.contentType(png))
        //assertEquals("image/png", mimeService.contentType(gifWrong));
        assertEquals("image/gif", mimeService.contentType(gif))
    }

    @BeforeEach
    fun setUp() {
        val test = File("test")
        if (!test.exists()) {
            test.mkdir()
        }
        val printStream = PrintStream("test/test.html")
        printStream.println("<!DOCTYPE html><html><head><title>Hello</title></head><body>Hello</body></html>")
        printStream.close()
        val http = Http()
        http.get("http://www.schaik.com/pngsuite/basn6a08.png").pipe(FileOutputStream("test/test.png")).execute()
        http.get("http://www.schaik.com/pngsuite/basn6a08.gif").pipe(FileOutputStream("test/test.gif")).execute()
        FileUtils.copyFile(File("test/test.png"), File("test/test_wrong.gif"))
    }


    @AfterEach
    fun tearDown() {
        FileUtils.deleteDirectory(File("test"))
    }


}