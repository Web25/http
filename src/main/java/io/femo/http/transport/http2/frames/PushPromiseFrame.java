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
public class PushPromiseFrame extends PaddedFrame {

    private int promisedStreamId;
    private byte[] headerBlockFragment;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public PushPromiseFrame(HttpSettings settings) {
        super(settings);
        setType(Constants.Http20.FrameType.PUSH_PROMISE);
    }

    public boolean isEndHeaders() {
        return (getFlags() & 0x4) == 0x4;
    }

    public void setEndHeaders(boolean endHeaders) {
        setFlags((short) (getFlags() & ~0x4 | (endHeaders ? 0x4 : 0)));
    }

    @Override
    public void build() {
        int length = headerBlockFragment.length + 4;    //stream identifier length
        if(isPadded()) {
            length++; //padding adds one byte
            length += getPadLength();
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        if(isPadded())
            buffer.put(HttpUtil.toByte(getPadLength()), 1, 1);
        buffer.putInt(promisedStreamId);
        buffer.put(headerBlockFragment);
        setPayload(buffer.array());
    }

    public int getPromisedStreamId() {
        return promisedStreamId;
    }

    public void setPromisedStreamId(int promisedStreamId) {
        this.promisedStreamId = promisedStreamId;
    }

    public byte[] getHeaderBlockFragment() {
        return headerBlockFragment;
    }

    public void setHeaderBlockFragment(byte[] headerBlockFragment) {
        this.headerBlockFragment = headerBlockFragment;
    }

    public static PushPromiseFrame from(HttpFrame frame) {
        PushPromiseFrame pushPromiseFrame = new PushPromiseFrame(frame.getSettings());
        pushPromiseFrame.setFlags(frame.getFlags());
        pushPromiseFrame.setStreamIdentifier(frame.getStreamIdentifier());
        ByteBuffer byteBuffer = ByteBuffer.wrap(frame.getPayload());
        if(pushPromiseFrame.isPadded()) {
            pushPromiseFrame.setPadLength((short) (byteBuffer.get() & 0xff));
        }
        pushPromiseFrame.setPromisedStreamId(byteBuffer.getInt());
        byte[] headerBlockFragment = new byte[byteBuffer.remaining() - pushPromiseFrame.getPadLength()];
        byteBuffer.get(headerBlockFragment);
        pushPromiseFrame.setHeaderBlockFragment(headerBlockFragment);
        if(byteBuffer.remaining() != pushPromiseFrame.getPadLength()) {
            throw new HttpFrameException("Invalid frame padding length");
        }
        return pushPromiseFrame;
    }
}
