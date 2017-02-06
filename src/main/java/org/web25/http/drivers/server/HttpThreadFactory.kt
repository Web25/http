package org.web25.http.drivers.server

import java.util.concurrent.ThreadFactory

/**
 * Created by Felix Resch on 29-Apr-16.
 */
class HttpThreadFactory(private val port: Int) : ThreadFactory {
    private var counter = 0

    override fun newThread(r: Runnable): Thread {
        val httpThread = HttpThread(r)
        httpThread.name = String.format("pool-%04d-thread-%03d", port, counter++)
        httpThread.isDaemon = true
        httpThread.priority = Thread.MAX_PRIORITY
        return httpThread
    }
}
