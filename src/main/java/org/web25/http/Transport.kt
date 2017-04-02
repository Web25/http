package org.web25.http

import org.web25.http.transport.HttpsTransport

import java.io.IOException
import java.net.Socket

/**
 * Created by felix on 9/10/15.
 */
interface Transport {

    @Throws(IOException::class)
    fun openSocket(host: String, port: Int): Socket

    companion object {

        val HTTP: Transport = org.web25.http.transport.HttpTransport()
        val HTTPS: Transport = HttpsTransport()
    }
}
