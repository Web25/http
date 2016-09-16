package io.femo.http.transport.http2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameReader {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

    private InputStream inputStream;
    private HttpConnection httpConnection;

    public HttpFrameReader(InputStream inputStream, HttpConnection httpConnection) {
        this.inputStream = inputStream;
        this.httpConnection = httpConnection;
    }

    public HttpFrame read() throws IOException {
        HttpFrame httpFrame = new HttpFrame(httpConnection.getLocalSettings());
        byte[] buffer = new byte[3];
        inputStream.read(buffer, 0, 3);
        httpFrame.setLength(HttpUtil.toInt(buffer));
        buffer = new byte[1];
        inputStream.read(buffer, 0, 1);
        httpFrame.setType(HttpUtil.toShort(buffer));
        inputStream.read(buffer, 0, 1);
        httpFrame.setFlags(HttpUtil.toShort(buffer));
        buffer = new byte[4];
        inputStream.read(buffer, 0, 4);
        httpFrame.setStreamIdentifier(HttpUtil.toInt(buffer));
        buffer = new byte[httpFrame.getLength()];
        inputStream.read(buffer, 0, buffer.length);
        httpFrame.setPayload(buffer);
        log.debug("Incoming frame: " + httpFrame.toString());
        return httpFrame;
    }
}
