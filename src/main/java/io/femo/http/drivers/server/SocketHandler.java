package io.femo.http.drivers.server;

import java.net.Socket;

/**
 * Created by felix on 9/15/16.
 */
public interface SocketHandler {

    void handle(Socket socket);
}
