package io.femo.http.transport.http2.frames;

import io.femo.http.transport.http2.HttpFrame;
import io.femo.http.transport.http2.HttpSettings;

/**
 * Created by felix on 9/18/16.
 */
public class EndStreamFrame extends HttpFrame {
    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public EndStreamFrame(HttpSettings settings) {
        super(settings);
    }

    public boolean isEndStream() {
        return (getFlags() & 1) == 1;
    }

    public void setEndStream(boolean endStream) {
        setFlags((short) (getFlags() & 0b11111110 | (endStream ? 1 : 0)));
    }
}
