package io.femo.http.transport.http2.frames;

import io.femo.http.Constants;
import io.femo.http.transport.http2.HttpFrame;
import io.femo.http.transport.http2.HttpFrameException;
import io.femo.http.transport.http2.HttpSettings;
import io.femo.http.transport.http2.HttpUtil;

import java.nio.ByteBuffer;

/**
 * Created by felix on 9/18/16.
 */
public class HeadersFrame extends PaddedFrame {

    private boolean e;
    private int streamDependency;
    private short weight;
    private byte[] headerBlockFragment;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public HeadersFrame(HttpSettings settings) {
        super(settings);
        setType(Constants.Http20.FrameType.HEADERS);
    }

    public boolean isEndHeaders() {
        return (getFlags() & 0x4) == 0x4;
    }

    public void setEndHeaders(boolean endHeaders) {
        setFlags((short) (getFlags() & ~0x4 | (endHeaders ? 0x4 : 0)));
    }

    public boolean isPriority() {
        return (getFlags() & 0x20) == 0x20;
    }

    public void setPriority(boolean priority) {
        setFlags((short) (getFlags() & ~0x20 | (priority ? 0x20 : 0)));
    }

    public boolean isE() {
        return e;
    }

    public void setE(boolean e) {
        this.e = e;
    }

    public int getStreamDependency() {
        return streamDependency;
    }

    public void setStreamDependency(int streamDependency) {
        this.streamDependency = streamDependency;
    }

    public short getWeight() {
        return weight;
    }

    public void setWeight(short weight) {
        this.weight = weight;
    }

    public byte[] getHeaderBlockFragment() {
        return headerBlockFragment;
    }

    public void setHeaderBlockFragment(byte[] headerBlockFragment) {
        this.headerBlockFragment = headerBlockFragment;
    }

    @Override
    public void build() {
        int length = headerBlockFragment.length;
        if(isPadded()) {
            length++; //padding adds one byte
            length += getPadLength();
        }
        if(isPriority()) {
            length += 5;
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        if(isPadded())
            buffer.put(HttpUtil.toByte(getPadLength()), 1, 1);
        if(isPriority()) {
            byte[] dep = HttpUtil.toByte(streamDependency);
            if(e) {
                dep[0] |= 0b10000000;
            }
            buffer.put(dep);
            buffer.put(HttpUtil.toByte(weight), 1, 1);
        }
        buffer.put(headerBlockFragment);
        setPayload(buffer.array());
    }

    public static HeadersFrame from(HttpFrame frame) {
        HeadersFrame headersFrame = new HeadersFrame(frame.getSettings());
        headersFrame.setFlags(frame.getFlags());
        headersFrame.setStreamIdentifier(frame.getStreamIdentifier());
        ByteBuffer byteBuffer = ByteBuffer.wrap(frame.getPayload());
        if(headersFrame.isPadded()) {
            headersFrame.setPadLength((short) (byteBuffer.get() & 0xff));
        }
        if(headersFrame.isPriority()) {
            byte[] data = new byte[4];
            byteBuffer.get(data);
            if(((data[0] & 0xff) & 128) == 128) {
                headersFrame.setE(true);
                data[0] = (byte) (data[0] & ~128);
            }
            headersFrame.setStreamDependency(HttpUtil.toInt(data));
            headersFrame.setWeight((short) (byteBuffer.get() & 0xff));
        }
        byte[] headerBlockFragment = new byte[byteBuffer.remaining() - headersFrame.getPadLength()];
        byteBuffer.get(headerBlockFragment);
        headersFrame.setHeaderBlockFragment(headerBlockFragment);
        if(byteBuffer.remaining() != headersFrame.getPadLength()) {
            throw new HttpFrameException("Invalid frame padding length");
        }
        return headersFrame;
    }
}
