package io.femo.http.transport.http2;

import org.jetbrains.annotations.Contract;

import java.io.IOException;

/**
 * Created by felix on 9/3/16.
 */
public class HttpStream {

    private int streamIdentifier;
    private HttpConnection httpConnection;
    private FlowControlWindow flowControlWindow;

    private State state;

    public HttpStream(HttpConnection httpConnection, int streamIdentifier) {
        this.httpConnection = httpConnection;
        this.streamIdentifier = streamIdentifier;
        this.state = State.IDLE;
    }

    @Contract(pure = true)
    private boolean isClosed() {
        return state != State.CLOSED;
    }

    public void sendFrame(HttpFrame httpFrame) throws IOException {
        httpConnection.enqueueFrame(httpFrame, this);
    }

    public int getStreamIdentifier() {
        return streamIdentifier;
    }

    public HttpConnection getHttpConnection() {
        return httpConnection;
    }

    public FlowControlWindow getFlowControlWindow() {
        return flowControlWindow;
    }

    public State getState() {
        return state;
    }

    public void handleFrame(HttpFrame httpFrame) {

    }

    public enum State {
        IDLE, RESERVED_LOCAL, RESERVED_REMOTE, OPEN, HALF_CLOSED_REMOTE, HALF_CLOSED_LOCAL, CLOSED
    }
}
