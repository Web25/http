package io.femo.http.transport.http2.frames;

import io.femo.http.transport.http2.HttpFrameException;
import io.femo.http.transport.http2.HttpSettings;

/**
 * Created by felix on 9/18/16.
 */
public class PaddedFrame extends EndStreamFrame {

    private short padLength;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public PaddedFrame(HttpSettings settings) {
        super(settings);
    }

    public boolean isPadded() {
        return (getFlags() & 0x8) == 0x8;
    }

    public void setPadded(boolean padded) {
        setFlags((short) (getFlags() & ~0x8 | (padded ? 0x8 : 0)));
    }

    public short getPadLength() {
        return padLength;
    }

    public void setPadLength(short padLength) {
        if(padLength > 255) {
            throw new HttpFrameException("Invalid value for padding");
        }
        this.padLength = padLength;
    }
}
