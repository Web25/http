package io.femo.http.transport.http2;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameWriterTest {

    /*@Test
    public void testWrite() throws Exception {
        HttpSettings settings = new HttpSettings();
        HttpFrame frame = new HttpFrame(settings);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HttpFrameWriter frameWriter = new HttpFrameWriter(byteArrayOutputStream, settings);
        frame.setPayload(new byte[] {1, 2, 3, 4, 5});
        frameWriter.write(frame);
        assertEquals("Length of Frame", 9 + 5, byteArrayOutputStream.size());
        assertArrayEquals("Content of Frame", new byte[]{0, 0, 5, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5}, byteArrayOutputStream.toByteArray());
    }*/
}