package org.web25.http.transport.http2

import org.junit.Test

/**
 * Created by felix on 9/3/16.
 */
class HttpFrameTest {

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testBiggerThanInSettings() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        settings.maxFrameSize = 20
        val frame = HttpFrame(settings)
        frame.length = 30
    }

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testBiggerThanInSettingsDefault() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        frame.length = 17000
    }

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testBiggerThanMaxPossible() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        settings.maxFrameSize = Integer.MAX_VALUE
        val frame = HttpFrame(settings)
        frame.length = Integer.MAX_VALUE
    }

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testTypeLimitations() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        frame.type = 300.toShort()
    }

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testTypeNegative() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        frame.type = (-12).toShort()
    }

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testFlagsLimitations() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        frame.flags = 300.toShort()
    }

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testFlagsNegative() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        frame.flags = (-12).toShort()
    }

    @Test(expected = HttpFrameException::class)
    @Throws(Exception::class)
    fun testStreamIdentifierNegative() {
        val settings = HttpSettings(HttpSettings.EndpointType.SERVER)
        val frame = HttpFrame(settings)
        frame.streamIdentifier = -12
    }
}