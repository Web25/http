package org.web25.http.transport.http2

/**
 * Created by felix on 9/3/16.
 */
class HttpFrameReaderTest/*@Test
    public void testRead() throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[]{0, 0, 5, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5});
        HttpSettings httpSettings = new HttpSettings();
        HttpFrameReader frameReader = new HttpFrameReader(byteArrayInputStream, httpSettings);
        HttpFrame frame = frameReader.read();
        assertEquals(5, frame.getLength());
        assertEquals(0, frame.getFlags());
        assertEquals(0, frame.getType());
        assertEquals(0, frame.getStreamIdentifier());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, frame.getPayload());
    }*/