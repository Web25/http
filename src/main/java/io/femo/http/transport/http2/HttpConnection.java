package io.femo.http.transport.http2;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import io.femo.http.Constants;

import java.io.IOException;
import java.net.Socket;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by felix on 9/3/16.
 */
public class HttpConnection {

    private HttpSettings remoteSettings;
    private HttpSettings localSettings;
    private HttpFrameWriter frameWriter;
    private HttpFrameReader frameReader;

    private FlowControlWindow flowControlWindow;

    private final TreeMap<Integer, HttpStream> httpStreams;

    private final ConcurrentLinkedQueue<HttpFrame> frameQueue;

    public HttpConnection(Socket socket) throws IOException {
        this.localSettings = new HttpSettings();
        this.frameWriter = new HttpFrameWriter(socket.getOutputStream(), this);
        this.frameReader = new HttpFrameReader(socket.getInputStream(), this);
        this.httpStreams = new TreeMap<>();
        this.frameQueue = new ConcurrentLinkedQueue<>();
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

                            } else {

                            }
                        } else if(httpStreams.containsKey(frame.getStreamIdentifier())) {
                            httpStreams.get(frame.getStreamIdentifier()).handleFrame(frame);
                        } else {
                            HttpStream httpStream = new HttpStream(HttpConnection.this, frame.getStreamIdentifier());
                            httpStreams.put(frame.getStreamIdentifier(), httpStream);
                            httpStream.handleFrame(frame);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
