package org.web25.http

/**
 * Created by felix on 6/3/16.
 */
class TestConstants {

    object HTTP {

        val HOST = if (System.getenv("HTTPBIN_HOST") == null) "httpbin.org" else System.getenv("HTTPBIN_HOST")
    }
}
