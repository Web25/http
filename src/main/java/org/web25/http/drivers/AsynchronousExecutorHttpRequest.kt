package org.web25.http.drivers

import org.web25.http.HttpContext
import org.web25.http.client.HttpResponseCallback
import org.web25.http.client.OutgoingHttpRequest
import java.util.concurrent.ExecutorService

/**
 * Created by felix on 2/9/16.
 */
class AsynchronousExecutorHttpRequest(private val executorService: ExecutorService, context : HttpContext) : AsynchronousHttpRequest(context) {

    override fun execute(callback: HttpResponseCallback?): OutgoingHttpRequest {
        executorService.submit(getRunnable(callback))
        return this
    }
}
