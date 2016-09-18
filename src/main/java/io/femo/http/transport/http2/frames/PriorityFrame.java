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
public class PriorityFrame extends HttpFrame {

    private boolean e;
    private int streamDependency;
    private short weight;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public PriorityFrame(HttpSettings settings) {
        super(settings);
        setType(Constants.Http20.FrameType.PRIORITY);
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

    @Override
    public void build() {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        byte[] dep = HttpUtil.toByte(streamDependency);
        if(e) {
            dep[0] |= 0b10000000;
        }
        buffer.put(dep);
        buffer.put(HttpUtil.toByte(weight), 1, 1);
        setPayload(buffer.array());
    }

    public static PriorityFrame from(HttpFrame frame) {
        PriorityFrame priorityFrame = new PriorityFrame(frame.getSettings());
        priorityFrame.setFlags(frame.getFlags());
        priorityFrame.setStreamIdentifier(frame.getStreamIdentifier());
        ByteBuffer byteBuffer = ByteBuffer.wrap(frame.getPayload());
        byte[] data = new byte[4];
        byteBuffer.get(data);
        if(((data[0] & 0xff) & 128) == 128) {
            priorityFrame.setE(true);
            data[0] = (byte) (data[0] & ~128);
        }
        priorityFrame.setStreamDependency(HttpUtil.toInt(data));
        priorityFrame.setWeight((short) (byteBuffer.get() & 0xff));
        return priorityFrame;
    }
}
