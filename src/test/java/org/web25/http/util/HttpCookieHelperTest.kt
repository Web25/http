package org.web25.http.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Created by felix on 2/18/17.
 */
internal class HttpCookieHelperTest {
    @Test
    fun defaultCookiePath() {
        assertEquals("/", HttpCookieHelper.defaultCookiePath("/index.html"))
        assertEquals("/", HttpCookieHelper.defaultCookiePath("/"))
        assertEquals("/", HttpCookieHelper.defaultCookiePath("/query"))
        assertEquals("/hello", HttpCookieHelper.defaultCookiePath("/hello/world"))
        assertEquals("/", HttpCookieHelper.defaultCookiePath("hello"))
    }

}