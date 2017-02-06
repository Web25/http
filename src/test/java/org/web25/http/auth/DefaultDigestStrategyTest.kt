package org.web25.http.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.web25.http.Http
import org.web25.http.drivers.DefaultHttpResponse

/**
 * Created by felix on 6/26/16.
 */
class DefaultDigestStrategyTest {

    @Test
    @Throws(Exception::class)
    fun testExample() {
        val httpRequest = Http["http://www.nowhere.org/dir/index.html"]
        val httpResponse = DefaultHttpResponse()
        httpResponse.header("WWW-Authenticate", "Digest " +
                "realm=\"testrealm@host.com\", " +
                "qop=\"auth,auth-int\", " +
                "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
                "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"")
        httpResponse.status(401)
        httpResponse.request(httpRequest)
        val defaultDigestStrategy = DefaultDigestStrategy("Mufasa", "Circle Of Life")
        defaultDigestStrategy.init(httpResponse)
        assertTrue(defaultDigestStrategy.isInitialized)
        assertTrue(defaultDigestStrategy.matches(httpRequest))
        defaultDigestStrategy.authenticate(httpRequest)
        assertEquals(httpRequest.header("Authorization").value, "Digest username=\"Mufasa\", " +
                "realm=\"testrealm@host.com\", " +
                "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
                "uri=\"/dir/index.html\", " +
                "qop=auth, " +
                "nc=00000001, " +
                "cnonce=\"0a4f113b\", " +
                "response=\"6629fae49393a05397450978507c4ef1\", " +
                "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"")
    }
}