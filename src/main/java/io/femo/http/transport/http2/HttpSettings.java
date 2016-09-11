package io.femo.http.transport.http2;

/**
 * Created by felix on 9/3/16.
 */
public class HttpSettings {

    private int maxFrameSize = 16384;

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public void setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }
}
