package io.femo.http.transport.http2.frames;

import io.femo.http.Constants;
import io.femo.http.transport.http2.HttpFrame;
import io.femo.http.transport.http2.HttpFrameException;
import io.femo.http.transport.http2.HttpSettings;
import io.femo.http.transport.http2.HttpUtil;

import javax.xml.crypto.Data;
import java.nio.ByteBuffer;

/**
 * Created by felix on 9/18/16.
 */
public class DataFrame extends PaddedFrame {

    private byte[] data;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public DataFrame(HttpSettings settings) {
        super(settings);
        setType(Constants.Http20.FrameType.DATA);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void build() {
        int length = data.length;
        if(isPadded()) {
            length ++;  //padding adds one byte
            length += getPadLength();
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        if(isPadded())
            buffer.put(HttpUtil.toByte(getPadLength()), 1, 1);
        buffer.put(data);
        setPayload(buffer.array());
    }

    public static DataFrame from(HttpFrame frame) {
        DataFrame dataFrame = new DataFrame(frame.getSettings());
        dataFrame.setFlags(frame.getFlags());
        dataFrame.setStreamIdentifier(frame.getStreamIdentifier());
        ByteBuffer byteBuffer = ByteBuffer.wrap(frame.getPayload());
        if(dataFrame.isPadded()) {
            dataFrame.setPadLength((short) (byteBuffer.get() & 0xff));
        }
        byte[] data = new byte[byteBuffer.remaining() - dataFrame.getPadLength()];
        byteBuffer.get(data);
        dataFrame.setData(data);
        if(byteBuffer.remaining() != dataFrame.getPadLength()) {
            throw new HttpFrameException("Invalid frame padding length");
        }
        return dataFrame;
    }
}
