package io.femo.http.transport.http2;

import io.femo.http.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameWriter {

    private OutputStream outputStream;
    private HttpConnection httpConnection;

    public HttpFrameWriter(OutputStream outputStream, HttpConnection httpConnection) {
        this.outputStream = outputStream;
        this.httpConnection = httpConnection;
    }

    public void write(HttpFrame httpFrame) throws IOException {
        httpFrame.build();
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(httpFrame.getLength() + Constants.Http20.FRAME_HEADER_LENGTH);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getLength()), 1, 3);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getType()), 1, 1);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getFlags()), 1, 1);
        outputBuffer.write(HttpUtil.toByte(httpFrame.getStreamIdentifier()));
        outputBuffer.write(httpFrame.getPayload());
        outputBuffer.writeTo(this.outputStream);
    }
}
