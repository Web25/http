package io.femo.http.drivers.server;

import io.femo.http.HttpVersion;
import org.eclipse.jetty.alpn.ALPN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by felix on 9/14/16.
 */
public class HttpsServerThread extends HttpServerThread {

    private Logger log = LoggerFactory.getLogger("HTTP");

    private SSLServerSocket sslServerSocket;
    private HttpHandlerStack httpHandlerStack;

    private int port;
    private boolean ready = false;
    private final Object lock = new Object();



    private SSLContext sslContext;

    public HttpsServerThread(HttpHandlerStack httpHandlerStack, SSLContext sslContext) throws NoSuchAlgorithmException, KeyManagementException {
        super(httpHandlerStack);
        this.httpHandlerStack = httpHandlerStack;
        this.sslContext = sslContext;
        setName("HTTPS-" + port);
    }

    @Override
    public void run() {
        log.debug("Starting Secure HTTP Server on port " + port);
        try {
            this.sslServerSocket = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(port);
        } catch (IOException e) {
            log.error("Error while starting Secure HTTP server on port " + port, e);
        }
        log.debug("Starting Executor Service");
        ExecutorService executorService = Executors.newCachedThreadPool(new HttpThreadFactory(port));
        while (!isInterrupted()) {
            synchronized (lock) {
                this.ready = true;
            }
            try {
                SSLSocket socket = (SSLSocket) sslServerSocket.accept();
                final AtomicReference<HttpVersion> httpVersion = new AtomicReference<>(HttpVersion.HTTP_11);
                ALPN.put(socket, new ALPN.ServerProvider() {
                    @Override
                    public void unsupported() {
                        ALPN.remove(socket);
                    }

                    @Override
                    public String select(List<String> list) throws SSLException {
                        ALPN.remove(socket);   //TODO swap ifs, as soon as http/2.0 is implemented
                        if(list.contains("h2")) {
                            httpVersion.set(HttpVersion.HTTP_20);
                            return "h2";
                        }
                        if(list.contains("http/1.1"))
                            return "http/1.1";
                        return list.get(0);
                    }
                });
                socket.startHandshake();        //necessary for ALPN to be evaluated
                executorService.submit(new SocketHandlerRunnable(socket, httpVersion.get()));
            } catch (SocketTimeoutException e) {
                log.debug("Socket timeout");
            } catch (SocketException e) {
                log.error("Socket has been closed", e);
                interrupt();
            } catch (IOException e) {
                log.warn("Socket Error", e);
            }
        }
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Had to perform dirty shutdown, not all clients might have been served!", e);
        }
    }

    @Override
    public void interrupt() {
        log.debug("Stopping HTTP Server on port {}", port);
        super.interrupt();
        try {
            sslServerSocket.close();
        } catch (IOException e) {
            log.error("Exception while shutting down server", e);
        }
    }

    public int getPort() {
        return port;
    }

    public boolean ready() {
        synchronized (lock) {
            return this.ready;
        }
    }

    public void setPort(int port) {
        setName("HTTP-" + port);
        this.port = port;
    }

    private class SocketHandlerRunnable implements Runnable {

        private SSLSocket socket;
        private HttpVersion httpVersion;

        private SocketHandler socketHandler;

        private SocketHandlerRunnable(SSLSocket socket) {
            this(socket, HttpVersion.HTTP_11);
        }

        private SocketHandlerRunnable(SSLSocket socket, HttpVersion httpVersion) {
            this.socket = socket;
            this.httpVersion = httpVersion;
        }

        @Override
        public void run() {
            if(httpVersion == HttpVersion.HTTP_11) {
                socketHandler = new Http11SocketHandler(httpHandlerStack);
            } else if (httpVersion == HttpVersion.HTTP_20) {
                socketHandler = new Http20SocketHandler(httpHandlerStack);
            }
            socketHandler.handle(socket);
        }
    }
}
