package io.femo.http.transport.http2;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import io.femo.http.Constants;
import io.femo.http.drivers.server.HttpHandlerStack;
import io.femo.http.transport.http2.frames.GoAwayFrame;
import io.femo.http.transport.http2.frames.SettingsFrame;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by felix on 9/3/16.
 */
public class HttpConnection {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

    private HttpSettings remoteSettings;
    private HttpSettings localSettings;
    private HttpFrameWriter frameWriter;
    private HttpFrameReader frameReader;

    private AtomicReference<Decoder> headerCompressionDecoder;
    private AtomicReference<Encoder> headerCompressionEncoder;

    private AtomicInteger lastRemote = new AtomicInteger(0);
    private final AtomicInteger nextStreamIdentifier;

    private AtomicLong updateTime = new AtomicLong(-1);

    private State state;

    private FlowControlWindow flowControlWindow;

    private final TreeMap<Integer, HttpStream> httpStreams;

    private final ConcurrentLinkedQueue<HttpFrame> frameQueue;

    public HttpConnection(Socket socket, HttpSettings httpSettings) throws IOException {
        this.localSettings = httpSettings;
        this.remoteSettings = new HttpSettings(HttpSettings.EndpointType.CLIENT);
        if(httpSettings.getEndpointType() == HttpSettings.EndpointType.CLIENT) {
            nextStreamIdentifier = new AtomicInteger(2);
        } else if (httpSettings.getEndpointType() == HttpSettings.EndpointType.SERVER) {
            nextStreamIdentifier = new AtomicInteger(1);
        } else {
            if(httpSettings.isInitiator()) {
                nextStreamIdentifier = new AtomicInteger(2);
            } else {
                nextStreamIdentifier = new AtomicInteger(1);
            }
        }
        this.frameWriter = new HttpFrameWriter(socket.getOutputStream(), this);
        this.frameReader = new HttpFrameReader(socket.getInputStream(), this);
        this.httpStreams = new TreeMap<>();
        this.frameQueue = new ConcurrentLinkedQueue<>();
        //this.headerCompressionDecoder = new AtomicReference<>(new Decoder())
        //this.headerCompressionEncoder = new AtomicReference<>(new Encoder())
        this.state = State.ACTIVE;
    }

    public synchronized void enqueueFrame(HttpFrame frame, HttpStream httpStream) throws IOException {
        if(frame.getType() == Constants.Http20.FrameType.SETTINGS) {
            if(frame.getFlags() == 0) {
                updateTime.set(System.currentTimeMillis());
            }
        }
        if(frame.getStreamIdentifier() == 0) {
            frameWriter.write(frame);
        } else if(frame.getType() == Constants.Http20.FrameType.DATA) {
            if(httpStream.getFlowControlWindow().checkOutgoing(frame.getLength())) {
                httpStream.getFlowControlWindow().decreaseRemote(frame.getLength());
                frameWriter.write(frame);
            } else {
                frameQueue.add(frame);
            }
        } else {
            frameWriter.write(frame);
        }

    }

    public HttpStream openNewStream() {
        return new HttpStream(this, nextStreamIdentifier.getAndAdd(2));
    }

    public HttpSettings getRemoteSettings() {
        return remoteSettings;
    }

    public HttpSettings getLocalSettings() {
        return localSettings;
    }

    public void handler(HttpHandlerStack httpHandlerStack) {

    }

    public Decoder getHpackDecoder() {
        return headerCompressionDecoder.get();
    }

    public void ping() throws IOException {
        HttpFrame ping = new HttpFrame(remoteSettings);
        ping.setType(Constants.Http20.FrameType.PING);
        ping.setPayload(RandomUtils.nextBytes(8));
        enqueueFrame(ping, null);
    }

    public void handle() {
        log.debug("Starting handling of connection");
        HttpFrame frame;
        try {
            frame = frameReader.read();
        } catch (IOException e) {
            log.warn("Error while reading handshake setting frame!");
            return;
        }
        if(frame.getType() != Constants.Http20.FrameType.SETTINGS) {
            log.warn("First frame wasn't a settings frame. Protocol violation");
            return;
        }
        try {
            remoteSettings.apply(SettingsFrame.from(frame));
            SettingsFrame settingsFrame = new SettingsFrame(remoteSettings);
            settingsFrame.setAck(true);
            enqueueFrame(settingsFrame, null);
        } catch (Http20Exception e) {
            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
            return;
        } catch (IOException e) {
            log.error("Error while sending settings ack");
            return;
        }
        this.headerCompressionDecoder = new AtomicReference<>(new Decoder(localSettings.getMaxHeaderListSize(), localSettings.getHeaderTableSize()));
        this.headerCompressionEncoder = new AtomicReference<>(new Encoder(localSettings.getHeaderTableSize()));
        boolean run = true;
        while (run) {
            try {
                frame = frameReader.read();
                synchronized (httpStreams) {
                    if (frame.getLength() > localSettings.getMaxFrameSize()) {
                        log.debug("Reveiced invalidly sized frame");
                        if (frame.getType() == Constants.Http20.FrameType.HEADERS ||
                                frame.getType() == Constants.Http20.FrameType.PUSH_PROMISE ||
                                frame.getType() == Constants.Http20.FrameType.CONTINUATION ||
                                frame.getType() == Constants.Http20.FrameType.SETTINGS ||
                                frame.getStreamIdentifier() == 0) {
                            terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR);
                        } else {
                            HttpFrame rstFrame = new HttpFrame(remoteSettings);
                            rstFrame.setType(Constants.Http20.FrameType.RST_STREAM);
                            rstFrame.setStreamIdentifier(frame.getStreamIdentifier());
                            rstFrame.setPayload(HttpUtil.toByte(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR));
                            enqueueFrame(rstFrame, httpStreams.get(frame.getStreamIdentifier()));
                        }
                    } else if (frame.getStreamIdentifier() == 0) {
                        if (frame.getType() == Constants.Http20.FrameType.SETTINGS) {
                            if(frame.getLength() % 6 != 0) {
                                terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR);
                            }
                            SettingsFrame settingsFrame = SettingsFrame.from(frame);
                            if (settingsFrame.isAck() && frame.getLength() != 0) {
                                terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR);
                            } else if (settingsFrame.isAck()) {
                                long diff = System.currentTimeMillis() - updateTime.getAndSet(-1);
                                if (diff > 100) {
                                    log.warn("Settings ack took some time...");
                                }
                            } else {
                                try {
                                    remoteSettings.apply(settingsFrame);
                                } catch (Http20Exception e) {
                                    log.warn("Invalid settings", e);
                                    terminate(e.getErrorCode());
                                }
                                SettingsFrame sFrame = new SettingsFrame(remoteSettings);
                                sFrame.setAck(true);
                                enqueueFrame(sFrame, null);
                            }
                        } else if (frame.getType() == Constants.Http20.FrameType.WINDOW_UPDATE) {
                            flowControlWindow.incremetRemote(HttpUtil.toInt(frame.getPayload()));
                        } else if (frame.getType() == Constants.Http20.FrameType.PING) {
                            if(frame.getFlags() == 1) {
                                log.debug("Ping acknowledged.");
                            } else {
                                if(frame.getLength() != 8) {
                                    terminate(Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR);
                                    break;
                                }
                                HttpFrame pong = new HttpFrame(remoteSettings);
                                pong.setType(Constants.Http20.FrameType.PING);
                                pong.setPayload(frame.getPayload());
                                pong.setFlags((short) 1);
                                enqueueFrame(pong, null);
                            }
                        } else {
                            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                            break;
                        }
                    } else if (httpStreams.containsKey(frame.getStreamIdentifier())) {
                        httpStreams.get(frame.getStreamIdentifier()).handleFrame(frame);
                    } else {
                        if (HttpConnection.this.state == State.CLOSE_PENDING) {
                            log.info("Connection is shutting down, dropping new incoming stream with id " + frame.getStreamIdentifier());
                        } else if (frame.getType() == Constants.Http20.FrameType.RST_STREAM) {
                            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR, "Protocol violation! A stream cannot be closed in IDLE state. Shutting down connection!");
                        } else if (frame.getType() == Constants.Http20.FrameType.DATA ||
                                frame.getType() == Constants.Http20.FrameType.WINDOW_UPDATE ||
                                frame.getType() == Constants.Http20.FrameType.CONTINUATION) {
                            terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                        } else {
                            if(remoteSettings.getEndpointType() == HttpSettings.EndpointType.CLIENT || remoteSettings.isInitiator()) {
                                if(frame.getStreamIdentifier() % 2 == 0) {
                                    terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                                }
                            }
                            HttpStream httpStream = new HttpStream(this, frame.getStreamIdentifier());
                            lastRemote.set(frame.getStreamIdentifier());
                            httpStreams.put(frame.getStreamIdentifier(), httpStream);
                            httpStream.handleFrame(frame);
                        }
                    }
                }
            } catch (SocketException e) {
                log.debug("Socket closed");
                run = false;
            } catch (IOException e) {
                log.warn("Error during connection handling", e);
                run = false;
            }
            if(state == State.CLOSE || state == State.FORCE_CLOSED) {
                run = false;
            }
        }
    }

    public class HttpConnectionReceiver implements Runnable {

        @Override
        public void run() {
            handle();
        }
    }

    public void terminate(int errorCode) {
        terminate(errorCode, (byte[]) null);
    }

    public void terminate(int errorCode, String debugData) {
        terminate(errorCode, debugData.getBytes());
    }

    public void terminate(int errorCode, byte[] debugData) {
        GoAwayFrame goAwayFrame = new GoAwayFrame(remoteSettings);
        goAwayFrame.setErrorCode(errorCode);
        goAwayFrame.setLastStreamId(lastRemote.get());
        goAwayFrame.setDebugData(debugData);
        try {
            frameWriter.write(goAwayFrame);
        } catch (IOException e) {
            log.warn("Could not write GOAWAY frame");
            this.state = State.FORCE_CLOSED;
        }
        this.state = State.CLOSE_PENDING;
    }

    public enum State {
        ACTIVE, CLOSE_PENDING, CLOSE, FORCE_CLOSED
    }
}
