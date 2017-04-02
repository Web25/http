package org.web25.http.drivers.server

import org.slf4j.LoggerFactory
import org.web25.http.HttpContext
import org.web25.http.drivers.treehandler.TreeHandler
import org.web25.http.transport.http2.HttpConnection
import org.web25.http.transport.http2.HttpSettings
import org.web25.http.transport.http2.frames.SettingsFrame
import java.io.IOException
import java.net.Socket

/**
 * Created by felix on 9/15/16.
 */
class TreeHttp20SocketHandler(private val treeHandler: TreeHandler, private val executorListener: ExecutorListener, val context : HttpContext) : SocketHandler {
    private val log = LoggerFactory.getLogger("HTTP")

    override fun handle(socket: Socket) {
        val preface = ByteArray(24)
        try {
            val inputStream = socket.inputStream
            inputStream.read(preface, 0, 24)
            if (String(preface) != "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n") {
                log.warn("Invalid HTTP/2.0 preface")
                socket.close()
                return
            }
            val httpConnection = HttpConnection(socket, HttpSettings(HttpSettings.EndpointType.SERVER), executorListener, context)
            httpConnection.handler(treeHandler)
            val settingsFrame = SettingsFrame(httpConnection.remoteSettings)
            httpConnection.enqueueFrame(settingsFrame, null)
            httpConnection.handle()
        } catch (e: IOException) {
            log.warn("Error in HTTP/2.0 connection", e)
        }

    }
}
