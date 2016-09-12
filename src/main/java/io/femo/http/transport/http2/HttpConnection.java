package io.femo.http.transport.http2;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import io.femo.http.Constants;
import io.femo.http.transport.http2.frames.GoAwayFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
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

    private AtomicInteger lastClient = new AtomicInteger(0);

    private State state;

    private FlowControlWindow flowControlWindow;

    private final TreeMap<Integer, HttpStream> httpStreams;

    private final ConcurrentLinkedQueue<HttpFrame> frameQueue;

    public HttpConnection(Socket socket) throws IOException {
        this.localSettings = new HttpSettings();
        this.frameWriter = new HttpFrameWriter(socket.getOutputStream(), this);
        this.frameReader = new HttpFrameReader(socket.getInputStream(), this);
        this.httpStreams = new TreeMap<>();
        this.frameQueue = new ConcurrentLinkedQueue<>();
        //this.headerCompressionDecoder = new AtomicReference<>(new Decoder())
        //this.headerCompressionEncoder = new AtomicReference<>(new Encoder())
        this.state = State.ACTIVE;
    }

    public synchronized void enqueueFrame(HttpFrame frame, HttpStream httpStream) throws IOException {
        if(frame.getType() == Constants.Http20.FrameType.DATA) {
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

    public HttpSettings getRemoteSettings() {
        return remoteSettings;
    }

    public HttpSettings getLocalSettings() {
        return localSettings;
    }

    public class HttpConnectionReceiver implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    HttpFrame frame = frameReader.read();
                    synchronized (httpStreams) {
                        if(frame.getStreamIdentifier() == 0) {
                            if(frame.getType() == Constants.Http20.FrameType.SETTINGS) {

                            } else if(frame.getType() == Constants.Http20.FrameType.WINDOW_UPDATE) {
                                flowControlWindow.incremetRemote(HttpUtil.toInt(frame.getPayload()));
                            } else {
                                terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR);
                            }
                        } else if(httpStreams.containsKey(frame.getStreamIdentifier())) {
                            httpStreams.get(frame.getStreamIdentifier()).handleFrame(frame);
                        } else {
                            if(HttpConnection.this.state == State.CLOSE_PENDING) {
                                log.info("Connection is shutting down, dropping new incoming stream with id " + frame.getStreamIdentifier());
                            } else if(frame.getType() == Constants.Http20.FrameType.RST_STREAM) {
                                terminate(Constants.Http20.ErrorCodes.PROTOCOL_ERROR, "Protocol violation! A stream cannot be closed in IDLE state. Shutting down connection!");
                            } else {
                                HttpStream httpStream = new HttpStream(HttpConnection.this, frame.getStreamIdentifier());
                                lastClient.set(frame.getStreamIdentifier());
                                httpStreams.put(frame.getStreamIdentifier(), httpStream);
                                httpStream.handleFrame(frame);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void terminate(int errorCode) throws IOException {
        terminate(errorCode, (byte[]) null);
    }

    public void terminate(int errorCode, String debugData) throws IOException {
        terminate(errorCode, debugData.getBytes());
    }

    public void terminate(int errorCode, byte[] debugData) throws IOException {
        GoAwayFrame goAwayFrame = new GoAwayFrame(remoteSettings);
        goAwayFrame.setErrorCode(errorCode);
        goAwayFrame.setLastStreamId(lastClient.get());
        goAwayFrame.setDebugData(debugData);
        frameWriter.write(goAwayFrame);
        this.state = State.CLOSE_PENDING;
    }

    public enum State {
        ACTIVE, CLOSE_PENDING, CLOSE
    }
}
