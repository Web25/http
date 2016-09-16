package io.femo.http.transport.http2.frames;

import io.femo.http.Constants;
import io.femo.http.transport.http2.HttpFrame;
import io.femo.http.transport.http2.HttpSettings;
import io.femo.http.transport.http2.HttpUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by felix on 9/12/16.
 */
public class GoAwayFrame extends HttpFrame {

    private int errorCode;
    private int lastStreamId;
    private byte[] debugData;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public GoAwayFrame(HttpSettings settings) {
        super(settings);
        setType(Constants.Http20.FrameType.GOAWAY);
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setLastStreamId(int lastStreamId) {
        this.lastStreamId = lastStreamId;
    }

    public int getLastStreamId() {
        return lastStreamId;
    }

    public void setDebugData(byte[] debugData) {
        this.debugData = debugData;
    }

    public byte[] getDebugData() {
        return debugData;
    }

    @Override
    public void build() {
        super.build();
        ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES + (debugData != null ? debugData.length : 0));
        buffer.put(HttpUtil.toByte(lastStreamId));
        buffer.put(HttpUtil.toByte(errorCode));
        if(debugData != null) {
            buffer.put(debugData);
        }
        setPayload(buffer.array());
    }
}
