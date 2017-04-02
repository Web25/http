package org.web25.http.transport.http2

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test


/**
 * Created by felix on 9/3/16.
 */
internal class HttpFrameTest {

    @Test
    fun testBiggerThanInSettings() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        settings.maxFrameSize = 20
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.length = 30
        })
    }

    @Test
    fun testBiggerThanInSettingsDefault() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.length = 17000
        })
    }

    @Test
    fun testBiggerThanMaxPossible() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        settings.maxFrameSize = Integer.MAX_VALUE
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.length = Integer.MAX_VALUE
        })
    }

    @Test
    fun testTypeLimitations() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.type = 300.toShort()
        })
    }

    @Test
    fun testTypeNegative() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.type = (-12).toShort()
        })
    }

    @Test
    fun testFlagsLimitations() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.flags = 300.toShort()
        })
    }

    @Test
    fun testFlagsNegative() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.flags = (-12).toShort()
        })
    }

    @Test
    fun testStreamIdentifierNegative() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        assertThrows<HttpFrameException>(HttpFrameException::class.java, {
            frame.streamIdentifier = -12
        })
    }
}