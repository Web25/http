package org.web25.http.drivers

import org.web25.http.HttpRequest

import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by felix on 2/9/16.
 */
class AsynchronousDriver : DefaultDriver {

    private val executorService: ExecutorService?

    constructor() {
        executorService = null
    }

    constructor(threads: Int) {
        this.executorService = Executors.newFixedThreadPool(threads)
    }

    override fun url(url: URL): HttpRequest {
        if (executorService == null)
            return AsynchronousHttpRequest().path(url.path).host(url.host).port(url.port)
        return AsynchronousExecutorHttpRequest(executorService).path(url.path).host(url.host).port(url.port)
    }
}
