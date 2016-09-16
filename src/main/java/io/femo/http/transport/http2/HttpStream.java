package io.femo.http.transport.http2;

import io.femo.http.Constants;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by felix on 9/3/16.
 */
public class HttpStream {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

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
        if(httpFrame.getStreamIdentifier() != streamIdentifier) {
            throw new HttpFrameException("Frame was delivered to wrong stream!");
        }
        if(httpFrame.getType() == Constants.Http20.FrameType.PING) {
            try {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            } catch (IOException e) {
                log.warn("Could not terminate connection properly", e);
                return;
            }
        }
        if(httpFrame.getType() == Constants.Http20.FrameType.GOAWAY) {
            try {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            } catch (IOException e) {
                log.error("Could not close connection after protocol violation", e);
            }
        } else if(httpFrame.getType() == Constants.Http20.FrameType.WINDOW_UPDATE) {
            int increment = HttpUtil.toInt(httpFrame.getPayload());
            if(increment <= 0) {
                log.warn("Stream error. WINDOW_UPDATE requires a value between 0 and 2^31. Terminating stream.");
                try {
                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                } catch (IOException e) {
                    log.error("Could not close stream", e);
                }
            } else {
                flowControlWindow.incremetRemote(HttpUtil.toInt(httpFrame.getPayload()));
            }
        }
    }

    public enum State {
        IDLE, RESERVED_LOCAL, RESERVED_REMOTE, OPEN, HALF_CLOSED_REMOTE, HALF_CLOSED_LOCAL, CLOSED
    }

    public void terminate(int errorCode) throws IOException {
        HttpFrame frame = new HttpFrame(httpConnection.getRemoteSettings());
        frame.setStreamIdentifier(streamIdentifier);
        frame.setType(Constants.Http20.FrameType.RST_STREAM);
        frame.setPayload(HttpUtil.toByte(errorCode));
        httpConnection.enqueueFrame(frame, this);
        state = State.CLOSED;
    }
}
