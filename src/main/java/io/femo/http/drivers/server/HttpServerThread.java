package io.femo.http.drivers.server;

import io.femo.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

/**
 * Created by Felix Resch on 25-Apr-16.
 */
public class HttpServerThread extends Thread {

    private Logger log = LoggerFactory.getLogger("HTTP");

    private ServerSocket serverSocket;
    private HttpHandlerStack httpHandlerStack;
    private int port;
    private boolean ready = false;
    private final Object lock = new Object();

    private ConcurrentLinkedQueue<Future<?>> futures;

    private static final int CONNECTION_TIMEOUT = 10000;

    public HttpServerThread(HttpHandlerStack httpHandlerStack) {
        this.httpHandlerStack = httpHandlerStack;
        this.futures = new ConcurrentLinkedQueue<>();
        setName("HTTP-" + port);
    }

    public void run() {
        log.debug("Starting HTTP Server on port {}", port);
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Error while starting HTTP service", e);
        }
        /*try {
            this.serverSocket.setSoTimeout(20000);
        } catch (SocketException e) {
            log.warn("Could not set timeout. Shutdown may lag a bit...", e);
        }*/
        log.debug("Starting Executor Service");
        ExecutorService executorService = Executors.newCachedThreadPool(new HttpThreadFactory(port));
        while (!isInterrupted()) {
            synchronized (lock) {
                this.ready = true;
            }
            try {
                Socket socket = serverSocket.accept();
                futures.add(executorService.submit(new SocketHandlerRunnable(socket)));
            } catch (SocketTimeoutException e) {
                log.debug("Socket timeout");
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
            serverSocket.close();
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

        private Socket socket;
        private HttpVersion httpVersion;
        private SocketHandler socketHandler;

        private SocketHandlerRunnable(Socket socket) {
            this(socket, HttpVersion.HTTP_11);
        }

        private SocketHandlerRunnable(Socket socket, HttpVersion httpVersion) {
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
