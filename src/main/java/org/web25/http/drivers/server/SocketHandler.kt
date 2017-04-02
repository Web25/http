package org.web25.http.drivers.server

import java.net.Socket

/**
 * Created by felix on 9/15/16.
 */
interface SocketHandler {

    fun handle(socket: Socket)
}
