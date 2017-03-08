package org.web25.http.drivers.server

import org.eclipse.jetty.alpn.ALPN
import org.slf4j.LoggerFactory
import org.web25.http.HttpContext
import org.web25.http.HttpVersion
import org.web25.http.drivers.treehandler.TreeHandler
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket

/**
 * Created by felix on 9/14/16.
 */
class TreeHttpsServerThread @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
constructor(private val treeHandler: TreeHandler, private val sslContext: SSLContext, context : HttpContext) : TreeHttpServerThread(treeHandler, context), ExecutorListener {

    private val log = LoggerFactory.getLogger("HTTP")

    private var sslServerSocket: SSLServerSocket? = null

    override var port: Int = 0
        set(port) {
            name = "HTTP-" + port
            field = port
        }
    private var ready = false
    private val lock = Any()

    private val futures: MutableList<Future<*>> = mutableListOf()

    private val futureWatchThread = FutureWatchThread()

    private var executorService: ExecutorService? = null

    init {
        name = "HTTPS-" + this.port
    }

    override fun run() {
        log.debug("Starting Secure HTTP Server on port " + this.port)
        try {
            this.sslServerSocket = sslContext.serverSocketFactory.createServerSocket(this.port) as SSLServerSocket
        } catch (e: IOException) {
            log.error("Error while starting Secure HTTP server on port " + this.port, e)
        }

        log.debug("Starting Executor Service")
        executorService = Executors.newCachedThreadPool(HttpThreadFactory(this.port))
        futureWatchThread.start()
        while (!isInterrupted) {
            synchronized(lock) {
                this.ready = true
            }
            try {
                val socket = sslServerSocket!!.accept() as SSLSocket
                val httpVersion = AtomicReference(HttpVersion.HTTP_11)
                ALPN.put(socket, object : ALPN.ServerProvider {
                    override fun unsupported() {
                        ALPN.remove(socket)
                    }

                    @Throws(SSLException::class)
                    override fun select(list: List<String>): String {
                        ALPN.remove(socket)   //TODO swap ifs, as soon as http/2.0 is implemented
                        if (list.contains("h2")) {
                            httpVersion.set(HttpVersion.HTTP_20)
                            log.debug("Using HTTP/2.0")
                            return "h2"
                        }
                        if (list.contains("http/1.1"))
                            return "http/1.1"
                        return list[0]
                    }
                })
                socket.startHandshake()        //necessary for ALPN to be evaluated
                synchronized(futures) {
                    futures.add(executorService!!.submit(SocketHandlerRunnable(socket, httpVersion.get())))
                }
            } catch (e: SocketTimeoutException) {
                log.debug("Socket timeout")
            } catch (e: SocketException) {
                log.error("Socket has been closed", e)
                interrupt()
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
        try {
            sslServerSocket!!.close()
        } catch (e: IOException) {
            log.error("Exception while shutting down server", e)
        }

        futureWatchThread.interrupt()
    }

    override fun ready(): Boolean {
        synchronized(lock) {
            return this.ready
        }
    }

    override fun submit(runnable: Runnable) {
        val future = executorService!!.submit(runnable)
        synchronized(futures) {
            this.futures.add(future)
        }
    }

    private inner class SocketHandlerRunnable internal constructor(private val socket: SSLSocket, private val httpVersion: HttpVersion = HttpVersion.HTTP_11) : Runnable {

        private var socketHandler: SocketHandler? = null

        override fun run() {
            if (httpVersion === HttpVersion.HTTP_11) {
                log.debug("Using HTTP/1.1")
                socketHandler = TreeHttp11SocketHandler(treeHandler, context)
            } else if (httpVersion === HttpVersion.HTTP_20) {
                log.debug("Using HTTP/2.0")
                socketHandler = TreeHttp20SocketHandler(treeHandler, this@TreeHttpsServerThread, context)
            }
            socketHandler!!.handle(socket)
        }
    }

    inner class FutureWatchThread : Thread() {

        override fun run() {
            try {
                while (!isInterrupted) {
                    Thread.sleep(1000)
                    synchronized(futures) {
                        val iterator = futures.iterator()
                        while (iterator.hasNext()) {
                            val future = iterator.next()
                            if (future.isDone) {
                                iterator.remove()
                                try {
                                    future.get()
                                } catch (e: InterruptedException) {
                                    log.warn("Error while handling socket", e)
                                } catch (e: ExecutionException) {
                                    log.warn("Error while handling socket", e)
                                }

                            }
                        }
                    }
                }
            } catch (e: InterruptedException) {
                log.warn("Future watch thread has been interrupted!", e)
            }

        }
    }
}
