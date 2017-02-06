package org.web25.http.drivers.server

import org.slf4j.LoggerFactory
import org.web25.http.HttpTransport
import org.web25.http.StatusCode
import org.web25.http.drivers.DefaultHttpResponse
import org.web25.http.helper.HttpHelper
import org.web25.http.helper.HttpSocketOptions
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * Created by felix on 9/15/16.
 */
class Http11SocketHandler(private val httpHandlerStack: HttpHandlerStack) : SocketHandler {

    private val log = LoggerFactory.getLogger("HTTP")

    override fun handle(socket: Socket) {
        var run = true
        try {
            socket.soTimeout = CONNECTION_TIMEOUT
        } catch (e: SocketException) {
            log.warn("Error while setting timeout!", e)
        }

        while (run) {
            try {
                run = false
                HttpHelper.get().add(HttpSocketOptions())
                val response = DefaultHttpResponse()
                val httpRequest = HttpTransport.def().readRequest(socket.inputStream)
                val start = System.currentTimeMillis()
                HttpHelper.remote(socket.remoteSocketAddress)
                HttpHelper.request(httpRequest)
                HttpHelper.response(response)
                HttpHelper.get().add(socket)
                httpHandlerStack.handle(httpRequest, response)
                if (httpRequest.hasHeader("Connection") && !response.hasHeader("Connection") && httpRequest.header("Connection").value == "keep-alive") {
                    response.header("Connection", "keep-alive")
                    run = true
                }
                val byteArrayOutputStream = ByteArrayOutputStream()
                response.print(byteArrayOutputStream)
                log.debug("Writing {} bytes to {}", byteArrayOutputStream.size(), socket.remoteSocketAddress.toString())
                byteArrayOutputStream.writeTo(socket.outputStream)
                socket.outputStream.flush()
                val httpSocketOptions = HttpHelper.get().getFirst<HttpSocketOptions>(HttpSocketOptions::class.java).get()
                if (httpSocketOptions.isClose)
                    socket.close()
                if (httpSocketOptions.hasHandledCallback()) {
                    httpSocketOptions.callHandledCallback()
                }
                log.info("Took {} ms to handle request", System.currentTimeMillis() - start)
                HttpHelper.get().reset()
            } catch (e: SSLHandshakeException) {
                log.warn("Error during handshake", e)
            } catch (e: SSLException) {
                log.warn("SSL Error", e)
                if (e.message!!.contains("plaintext")) {
                    val httpResponse = DefaultHttpResponse()
                    httpResponse.status(StatusCode.BAD_REQUEST)
                            .entity("You attempted a non secure connection on an secure port!")
                    try {
                        httpResponse.print(socket.outputStream)
                        socket.close()
                    } catch (e1: IOException) {
                        log.warn("Could not send error message", e1)
                    }

                }
            } catch (e: SocketTimeoutException) {
                log.debug("Connection timed out with " + socket.remoteSocketAddress.toString())
                try {
                    socket.close()
                } catch (e1: IOException) {
                    log.warn("Could not close socket", e1)
                }

            } catch (e: IOException) {
                log.warn("Socket Error", e)
            }

        }
    }

    companion object {


        private val CONNECTION_TIMEOUT = 10000
    }
}
