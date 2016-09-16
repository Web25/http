package io.femo.http.drivers.server;

import io.femo.http.HttpRequest;
import io.femo.http.HttpTransport;
import io.femo.http.StatusCode;
import io.femo.http.drivers.DefaultHttpResponse;
import io.femo.http.helper.HttpHelper;
import io.femo.http.helper.HttpSocketOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by felix on 9/15/16.
 */
public class Http11SocketHandler implements SocketHandler {

    private Logger log = LoggerFactory.getLogger("HTTP");

    private HttpHandlerStack httpHandlerStack;


    private static final int CONNECTION_TIMEOUT = 10000;

    public Http11SocketHandler(HttpHandlerStack httpHandlerStack) {
        this.httpHandlerStack = httpHandlerStack;
    }

    @Override
    public void handle(Socket socket) {
        boolean run = true;
        try {
            socket.setSoTimeout(CONNECTION_TIMEOUT);
        } catch (SocketException e) {
            log.warn("Error while setting timeout!", e);
        }
        while (run) {
            try {
                run = false;
                HttpHelper.get().add(new HttpSocketOptions());
                DefaultHttpResponse response = new DefaultHttpResponse();
                HttpRequest httpRequest = HttpTransport.def().readRequest(socket.getInputStream());
                long start = System.currentTimeMillis();
                HttpHelper.remote(socket.getRemoteSocketAddress());
                HttpHelper.request(httpRequest);
                HttpHelper.response(response);
                HttpHelper.get().add(socket);
                httpHandlerStack.handle(httpRequest, response);
                if (httpRequest.hasHeader("Connection") && !response.hasHeader("Connection") && httpRequest.header("Connection").value().equals("keep-alive")) {
                    response.header("Connection", "keep-alive");
                    run = true;
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                response.print(byteArrayOutputStream);
                log.debug("Writing {} bytes to {}", byteArrayOutputStream.size(), socket.getRemoteSocketAddress().toString());
                byteArrayOutputStream.writeTo(socket.getOutputStream());
                socket.getOutputStream().flush();
                HttpSocketOptions httpSocketOptions = HttpHelper.get().getFirst(HttpSocketOptions.class).get();
                if (httpSocketOptions.isClose())
                    socket.close();
                if (httpSocketOptions.hasHandledCallback()) {
                    httpSocketOptions.callHandledCallback();
                }
                log.info("Took {} ms to handle request", (System.currentTimeMillis() - start));
                HttpHelper.get().reset();
            } catch (SSLHandshakeException e) {
                log.warn("Error during handshake", e);
            } catch (SSLException e) {
                log.warn("SSL Error", e);
                if(e.getMessage().contains("plaintext")) {
                    DefaultHttpResponse httpResponse = new DefaultHttpResponse();
                    httpResponse.status(StatusCode.BAD_REQUEST)
                            .entity("You attempted a non secure connection on an secure port!");
                    try {
                        httpResponse.print(socket.getOutputStream());
                        socket.close();
                    } catch (IOException e1) {
                        log.warn("Could not send error message", e1);
                    }
                }
            } catch (SocketTimeoutException e) {
                log.debug("Connection timed out with " + socket.getRemoteSocketAddress().toString());
                try {
                    socket.close();
                } catch (IOException e1) {
                    log.warn("Could not close socket", e1);
                }
            } catch (IOException e) {
                log.warn("Socket Error", e);
            }
        }
    }
}
