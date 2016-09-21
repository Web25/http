package io.femo.http.transport.http2;

import io.femo.http.transport.http2.util.DebugInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameReader {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

    private InputStream inputStream;
    private HttpConnection httpConnection;

    public HttpFrameReader(InputStream inputStream, HttpConnection httpConnection) {
        this.inputStream = new DebugInputStream(inputStream);
        this.httpConnection = httpConnection;
    }

    public HttpFrame read() throws IOException {
        HttpFrame httpFrame = new HttpFrame(httpConnection.getLocalSettings());
        byte[] header = new byte[9];
        inputStream.read(header, 0, 9);
        byte buffer[];
        buffer = Arrays.copyOfRange(header, 0, 3);
        httpFrame.setLength(HttpUtil.toInt(buffer));
        buffer = Arrays.copyOfRange(header, 3, 4);
        httpFrame.setType(HttpUtil.toShort(buffer));
        buffer = Arrays.copyOfRange(header, 4, 5);
        httpFrame.setFlags(HttpUtil.toShort(buffer));
        buffer = Arrays.copyOfRange(header, 5, 9);
        httpFrame.setStreamIdentifier(HttpUtil.toInt(buffer));
        buffer = new byte[httpFrame.getLength()];
        inputStream.read(buffer, 0, buffer.length);
        httpFrame.setPayload(buffer);
        log.debug("Incoming frame: " + httpFrame.toString());
        return httpFrame;
    }
}
