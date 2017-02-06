package org.web25.http.transport

import org.web25.http.Transport
import java.io.IOException
import java.net.Socket
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

/**
 * Created by felix on 2/7/16.
 */
class HttpsTransport : Transport {

    @Throws(IOException::class)
    override fun openSocket(host: String, port: Int): Socket {
        return sslSocketFactory!!.createSocket(host, port)
    }

    companion object {

        private var sslSocketFactory: SocketFactory? = null

        init {
            sslSocketFactory = SSLSocketFactory.getDefault()
        }
    }
}
