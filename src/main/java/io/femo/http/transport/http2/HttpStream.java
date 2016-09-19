package io.femo.http.transport.http2;

import com.twitter.hpack.Encoder;
import com.twitter.hpack.HeaderListener;
import io.femo.http.Constants;
import io.femo.http.HttpRequest;
import io.femo.http.HttpResponse;
import io.femo.http.drivers.DefaultHttpRequest;
import io.femo.http.drivers.DefaultHttpResponse;
import io.femo.http.drivers.IncomingHttpRequest;
import io.femo.http.transport.http2.frames.DataFrame;
import io.femo.http.transport.http2.frames.HeadersFrame;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by felix on 9/3/16.
 */
public class HttpStream {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

    private int streamIdentifier;
    private HttpConnection httpConnection;
    private FlowControlWindow flowControlWindow;
    private boolean terminated;
    private boolean waitingForHeaders;
    private boolean endStream;

    private IncomingHttpRequest httpRequest;
    private HttpResponse httpResponse;

    private State state;

    public HttpStream(HttpConnection httpConnection, int streamIdentifier) {
        this.httpConnection = httpConnection;
        this.streamIdentifier = streamIdentifier;
        this.state = State.IDLE;
        this.flowControlWindow = new FlowControlWindow(httpConnection.getFlowControlWindow(), httpConnection);
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

    private void setState(State state) {
        log.debug("Stream {} transitioning from {} to {}", streamIdentifier, this.state, state);
        this.state = state;
    }

    public void handleFrame(HttpFrame httpFrame) {
        if(httpFrame.getStreamIdentifier() != streamIdentifier) {
            throw new HttpFrameException("Frame was delivered to wrong stream!");
        }
        if(httpFrame.getType() == Constants.Http20.FrameType.PING) {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
        } else if(httpFrame.getType() == Constants.Http20.FrameType.GOAWAY) {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
        }
        if(waitingForHeaders && httpFrame.getType() != Constants.Http20.FrameType.CONTINUATION) {
            httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            return;
        }
        if(httpFrame.getType() == Constants.Http20.FrameType.WINDOW_UPDATE) {
            int increment = HttpUtil.toInt(httpFrame.getPayload());
            if(increment <= 0) {
                log.warn("Stream error. WINDOW_UPDATE requires a value between 0 and 2^31. Terminating stream.");
                terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            } else {
                flowControlWindow.incremetRemote(HttpUtil.toInt(httpFrame.getPayload()));
            }
            return;
        }
        if(state == State.IDLE) {
            if(httpFrame.getType() == Constants.Http20.FrameType.HEADERS) {
                this.httpRequest = new IncomingHttpRequest();
                HeadersFrame headersFrame = HeadersFrame.from(httpFrame);
                try {
                    httpConnection.getHpackDecoder().decode(new ByteArrayInputStream(headersFrame.getHeaderBlockFragment()), new HeaderListener() {
                        @Override
                        public void addHeader(byte[] bytes, byte[] bytes1, boolean b) {
                            String name = new String(bytes);
                            String value = new String(bytes1);
                            log.debug("{} -> {}", name, value);
                            httpRequest.header(name, value);
                            if(name.startsWith(":")) {
                                if(name.equals(":method")) {
                                    httpRequest.method(value);
                                } else if (name.equals(":path")) {
                                    httpRequest.path(value);
                                } else if (name.equals(":authority")) {
                                    if(!httpRequest.hasHeader("host")) {
                                        httpRequest.header("host", value);
                                    }
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    log.warn("Error while decoding headers", e);
                    httpConnection.terminate(Constants.Http20.ErrorCodes.COMPRESION_ERROR);
                }
                if (headersFrame.isEndHeaders()) {
                    if (headersFrame.isEndStream()) {
                        setState(State.HALF_CLOSED_REMOTE);
                        handle();
                    } else {
                        setState(State.OPEN);
                    }
                } else {
                    if(headersFrame.isEndStream())
                        endStream = true;
                    this.waitingForHeaders = true;
                    httpConnection.exclusiveHeaderLock(this);
                }
            } else if (httpFrame.getType() == Constants.Http20.FrameType.CONTINUATION) {
                if(!waitingForHeaders) {
                    httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                    return;
                }
                try {
                    httpConnection.getHpackDecoder().decode(new ByteArrayInputStream(httpFrame.getPayload()), new HeaderListener() {
                        @Override
                        public void addHeader(byte[] bytes, byte[] bytes1, boolean b) {
                            log.debug("{} -> {}", new String(bytes), new String(bytes1));
                        }
                    });
                } catch (IOException e) {
                    log.warn("Error while decoding headers", e);
                    httpConnection.terminate(Constants.Http20.ErrorCodes.COMPRESION_ERROR);
                }
                if((httpFrame.getFlags() & 0x4) == 0x4) {
                    httpConnection.releaseLock(this);
                    log.debug(httpConnection.getHpackDecoder().endHeaderBlock() + "");
                    if(endStream) {
                        setState(State.HALF_CLOSED_REMOTE);
                    } else {
                        setState(State.OPEN);
                    }
                }
            } else if (httpFrame.getType() == Constants.Http20.FrameType.PRIORITY) {

            } else {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            }
        } else if (state == State.RESERVED_LOCAL) {
            if (httpFrame.getType() == Constants.Http20.FrameType.RST_STREAM) {
                setState(State.CLOSED);
            } else if (httpFrame.getType() == Constants.Http20.FrameType.PRIORITY) {

            } else {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            }
        } else if (state == State.RESERVED_REMOTE) {
            if(httpFrame.getType() == Constants.Http20.FrameType.HEADERS) {
                setState(State.HALF_CLOSED_REMOTE);
            } else if (httpFrame.getType() == Constants.Http20.FrameType.RST_STREAM) {
                setState(State.CLOSED);
            } else if (httpFrame.getType() == Constants.Http20.FrameType.PRIORITY) {

            } else {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            }
        } else if (state == State.OPEN) {
            if(httpFrame.hasEndStreamFlag()) {
                if((httpFrame.getFlags() & 0x1) == 0x1) {
                    this.setState(State.HALF_CLOSED_REMOTE);
                }
                if(httpFrame.getType() == Constants.Http20.FrameType.DATA) {
                    try {
                        DataFrame dataFrame = DataFrame.from(httpFrame);
                    } catch (HttpFrameException e) {
                        terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR);
                    }

                } else if (httpFrame.getType() == Constants.Http20.FrameType.HEADERS) {
                    try {
                        HeadersFrame headersFrame = HeadersFrame.from(httpFrame);
                    } catch (HttpFrameException e) {
                        httpConnection.terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR);
                    }
                }
            } else if(httpFrame.getType() == Constants.Http20.FrameType.RST_STREAM) {
                setState(State.CLOSED);
            } else if(httpFrame.getType() == Constants.Http20.FrameType.CONTINUATION) {
                httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                return;
            }
        } else if (state == State.HALF_CLOSED_LOCAL) {
            if(httpFrame.getType() == Constants.Http20.FrameType.RST_STREAM) {
                setState(State.CLOSED);
            }
        } else if (state == State.HALF_CLOSED_REMOTE) {
            if (httpFrame.getType() == Constants.Http20.FrameType.PRIORITY) {
                //TODO implement prioritization
            } else if (httpFrame.getType() == Constants.Http20.FrameType.RST_STREAM) {
                setState(State.CLOSED);
            } else {
                terminate(Constants.Http20.ErrorCodes.STREAM_CLOSED);
            }
        } else if (state == State.CLOSED) {
            if(httpFrame.getType() == Constants.Http20.FrameType.DATA) {
                flowControlWindow.decreaseLocal(httpFrame.getLength());
                terminate(Constants.Http20.ErrorCodes.STREAM_CLOSED);
            } else if (httpFrame.getType() == Constants.Http20.FrameType.PRIORITY) {
                //TODO implement prioritization
            } else if (httpFrame.getType() == Constants.Http20.FrameType.WINDOW_UPDATE) {
                log.debug("Ignoring window update on already closed stream");
            } else {
                if (!terminated) {
                    terminate(Constants.Http20.ErrorCodes.STREAM_CLOSED);
                }
            }
        }
        /* else if(httpFrame.getType() == Constants.Http20.FrameType.WINDOW_UPDATE) {
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
        } else if (httpFrame.getType() == Constants.Http20.FrameType.DATA) {
            if(state == State.OPEN || state == State.HALF_CLOSED_LOCAL) {

            } else {
                try {
                    terminate(Constants.Http20.ErrorCodes.STREAM_CLOSED);
                } catch (IOException e) {
                    log.warn("Could not terminate stream", e);
                    state = State.CLOSED;
                }
            }
        } else if (httpFrame.getType() == Constants.Http20.FrameType.HEADERS) {
            if(state == State.IDLE || state == State.RESERVED_REMOTE || state == State.OPEN || state == State.HALF_CLOSED_LOCAL ) {

            } else {
                try {
                    httpConnection.terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                } catch (IOException e) {
                    log.warn("Could not terminate connection");
                    state = State.CLOSED;
                }
            }
        }*/
    }

    private void handle() {
        this.httpResponse = new DefaultHttpResponse();
        httpConnection.deferHandling(httpRequest, httpResponse, this);
    }

    public void notfiyHandlingFinished() {
        Encoder encoder = httpConnection.getHpackEncoder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            encoder.encodeHeader(byteArrayOutputStream, ":status".getBytes(), String.valueOf(httpResponse.statusCode()).getBytes(), false);
            httpResponse.headers().forEach(httpHeader -> {
                try {
                    encoder.encodeHeader(byteArrayOutputStream, httpHeader.name().toLowerCase().getBytes(), httpHeader.value().getBytes(), false);
                } catch (IOException e) {
                    log.warn("Could not encode headers");
                }
            });
        } catch (IOException e) {
            log.warn("Could not encode headers");
        }
        HeadersFrame headersFrame = new HeadersFrame(httpConnection.getRemoteSettings());
        headersFrame.setEndHeaders(true);
        headersFrame.setStreamIdentifier(this.getStreamIdentifier());
        headersFrame.setHeaderBlockFragment(byteArrayOutputStream.toByteArray());
        try {
            httpConnection.enqueueFrame(headersFrame, this);
        } catch (IOException e) {
            log.warn("Could not respond to request", e);
        }
        DataFrame dataFrame = new DataFrame(httpConnection.getRemoteSettings());
        dataFrame.setData(httpResponse.responseBytes());
        dataFrame.setStreamIdentifier(this.getStreamIdentifier());
        dataFrame.setFlags((short) 0x1);
        try {
            httpConnection.enqueueFrame(dataFrame, this);
        } catch (IOException e) {
            log.warn("Could not respond to request", e);
        }
        setState(State.CLOSED);
    }

    public enum State {
        IDLE, RESERVED_LOCAL, RESERVED_REMOTE, OPEN, HALF_CLOSED_REMOTE, HALF_CLOSED_LOCAL, CLOSED
    }

    public void terminate(int errorCode) {
        HttpFrame frame = new HttpFrame(httpConnection.getRemoteSettings());
        frame.setStreamIdentifier(streamIdentifier);
        frame.setType(Constants.Http20.FrameType.RST_STREAM);
        frame.setPayload(HttpUtil.toByte(errorCode));
        try {
            httpConnection.enqueueFrame(frame, this);
        } catch (IOException e) {
            log.warn("Could not terminate stream", e);
        }
        terminated = true;
        setState(State.CLOSED);
    }
}
