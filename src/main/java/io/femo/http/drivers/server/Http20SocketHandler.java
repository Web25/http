package io.femo.http.drivers.server;

import io.femo.http.transport.http2.HttpConnection;
import io.femo.http.transport.http2.HttpSettings;
import io.femo.http.transport.http2.frames.SettingsFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by felix on 9/15/16.
 */
public class Http20SocketHandler implements SocketHandler {

    private Logger log = LoggerFactory.getLogger("HTTP");

    private HttpHandlerStack httpHandlerStack;

    public Http20SocketHandler(HttpHandlerStack httpHandlerStack) {
        this.httpHandlerStack = httpHandlerStack;
    }

    @Override
    public void handle(Socket socket) {
        byte[] preface = new byte[24];
        try {
            InputStream inputStream = socket.getInputStream();
            inputStream.read(preface, 0, 24);
            if(!new String(preface).equals("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n")) {
                log.warn("Invalid HTTP/2.0 preface");
                socket.close();
                return;
            }
            HttpConnection httpConnection = new HttpConnection(socket, new HttpSettings(HttpSettings.EndpointType.SERVER));
            httpConnection.handler(httpHandlerStack);
            SettingsFrame settingsFrame = new SettingsFrame(httpConnection.getRemoteSettings());
            httpConnection.enqueueFrame(settingsFrame, null);
            httpConnection.handle();
        } catch (IOException e) {
            log.warn("Error in HTTP/2.0 connection", e);
        }

    }
}
