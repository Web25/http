package org.web25.http.transport

import org.web25.http.Transport

import java.io.IOException
import java.net.Socket

/**
 * Created by felix on 2/7/16.
 */
class HttpTransport : Transport {

    @Throws(IOException::class)
    override fun openSocket(host: String, port: Int): Socket {
        return Socket(host, port)
    }
}
