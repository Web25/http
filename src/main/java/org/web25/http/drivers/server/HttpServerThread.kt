package org.web25.http.drivers.server

import org.slf4j.LoggerFactory
import org.web25.http.HttpContext
import org.web25.http.HttpVersion
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.*

/**
 * Created by Felix Resch on 25-Apr-16.
 */
open class HttpServerThread(private val httpHandlerStack: HttpHandlerStack, val context : HttpContext) : Thread(), ExecutorListener {

    private val log = LoggerFactory.getLogger("HTTP")

    private var serverSocket: ServerSocket? = null
    open var port: Int = 0
        set(port) {
            name = "HTTP-" + port
            field = port
        }
    private var ready = false
    private val lock = Any()

    private val futures: ConcurrentLinkedQueue<Future<*>>

    init {
        this.futures = ConcurrentLinkedQueue<Future<*>>()
        name = "HTTP-" + this.port
    }

    private var executorService: ExecutorService? = null

    override fun run() {
        log.debug("Starting HTTP Server on port {}", this.port)
        try {
            this.serverSocket = ServerSocket(this.port)
        } catch (e: IOException) {
            log.error("Error while starting HTTP service", e)
        }

        /*try {
            this.serverSocket.setSoTimeout(20000);
        } catch (SocketException e) {
            log.warn("Could not set timeout. Shutdown may lag a bit...", e);
        }*/
        log.debug("Starting Executor Service")
        executorService = Executors.newCachedThreadPool(HttpThreadFactory(this.port))
        while (!isInterrupted) {
            synchronized(lock) {
                this.ready = true
            }
            try {
                val socket = serverSocket!!.accept()
                log.debug("Incoming connection from ${socket.remoteSocketAddress}")
                futures.add(executorService!!.submit(SocketHandlerRunnable(socket)))
            } catch (e: SocketTimeoutException) {
                log.debug("Socket timeout")
            } catch (e: IOException) {
                log.warn("Socket Error", e)
            }

        }
        try {
            executorService!!.awaitTermination(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            log.warn("Had to perform dirty shutdown, not all clients might have been served!", e)
        }

    }

    override fun interrupt() {
        log.debug("Stopping HTTP Server on port {}", this.port)
        super.interrupt()
        if (serverSocket != null) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                log.error("Exception while shutting down server", e)
            }

        }
    }

    open fun ready(): Boolean {
        synchronized(lock) {
            return this.ready
        }
    }

    override fun submit(runnable: Runnable) {

    }

    private inner class SocketHandlerRunnable internal constructor(private val socket: Socket, private val httpVersion: HttpVersion = HttpVersion.HTTP_11) : Runnable {
        private var socketHandler: SocketHandler? = null

        override fun run() {
            log.debug("Starting handling of ${socket.remoteSocketAddress}")
            if (httpVersion === HttpVersion.HTTP_11) {
                log.debug("Using HTTP/1.1")
                socketHandler = Http11SocketHandler(httpHandlerStack, context)
            } else if (httpVersion === HttpVersion.HTTP_20) {
                log.debug("Using HTTP/2.0")
                socketHandler = Http20SocketHandler(httpHandlerStack, this@HttpServerThread, context)
            }
            socketHandler!!.handle(socket)
        }
    }

    companion object {

        private val CONNECTION_TIMEOUT = 10000
    }
}
