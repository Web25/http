package io.femo.http.transport.http2;

import io.femo.http.Constants;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameTest {

    @Test(expected = HttpFrameException.class)
    public void testBiggerThanInSettings() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        settings.setMaxFrameSize(20);
        HttpFrame frame = new HttpFrame(settings);
        frame.setLength(30);
    }

    @Test(expected = HttpFrameException.class)
    public void testBiggerThanInSettingsDefault() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        HttpFrame frame = new HttpFrame(settings);
        frame.setLength(17000);
    }
    @Test(expected = HttpFrameException.class)
    public void testBiggerThanMaxPossible() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        settings.setMaxFrameSize(Integer.MAX_VALUE);
        HttpFrame frame = new HttpFrame(settings);
        frame.setLength(Integer.MAX_VALUE);
    }

    @Test(expected = HttpFrameException.class)
    public void testTypeLimitations() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        HttpFrame frame = new HttpFrame(settings);
        frame.setType((short) 300);
    }

    @Test(expected = HttpFrameException.class)
    public void testTypeNegative() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        HttpFrame frame = new HttpFrame(settings);
        frame.setType((short) -12);
    }

    @Test(expected = HttpFrameException.class)
    public void testFlagsLimitations() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        HttpFrame frame = new HttpFrame(settings);
        frame.setFlags((short) 300);
    }

    @Test(expected = HttpFrameException.class)
    public void testFlagsNegative() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        HttpFrame frame = new HttpFrame(settings);
        frame.setFlags((short) -12);
    }

    @Test(expected = HttpFrameException.class)
    public void testStreamIdentifierNegative() throws Exception {
        HttpSettings settings = new HttpSettings(HttpSettings.EndpointType.SERVER);
        HttpFrame frame = new HttpFrame(settings);
        frame.setStreamIdentifier(-12);
    }
}