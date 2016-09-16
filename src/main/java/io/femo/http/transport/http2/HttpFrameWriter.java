package io.femo.http.transport.http2;

import io.femo.http.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameWriter {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

    private OutputStream outputStream;
    private HttpConnection httpConnection;

    public HttpFrameWriter(OutputStream outputStream, HttpConnection httpConnection) {
        this.outputStream = outputStream;
        this.httpConnection = httpConnection;
    }

    public void write(HttpFrame httpFrame) throws IOException {
        try {
            httpFrame.build();
        } catch (Throwable t) {
            log.error("Error while building frame", t);
        }
        log.debug("Writing frame: " + httpFrame.toString());
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(httpFrame.getLength() + Constants.Http20.FRAME_HEADER_LENGTH);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getLength()), 1, 3);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getType()), 1, 1);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getFlags()), 1, 1);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getStreamIdentifier()));
        outputBuffer.write(httpFrame.getPayload());
        outputBuffer.writeTo(this.outputStream);
    }
}
