package org.web25.http.drivers

import org.web25.http.HttpContext
import org.web25.http.client.OutgoingHttpRequest
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by felix on 2/9/16.
 */
class AsynchronousDriver : DefaultDriver {

    private val executorService: ExecutorService?

    constructor(context : HttpContext): super(context) {
        executorService = null
    }

    constructor(threads: Int, context : HttpContext): super(context) {
        this.executorService = Executors.newFixedThreadPool(threads)
    }

    override fun openRequest(url: URL): OutgoingHttpRequest {
        if (executorService == null)
            return AsynchronousHttpRequest(context).path(url.path).host(url.host).port(url.port)
        return AsynchronousExecutorHttpRequest(executorService, context).path(url.path).host(url.host).port(url.port)
    }
}
