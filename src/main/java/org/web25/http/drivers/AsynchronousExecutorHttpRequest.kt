package org.web25.http.drivers

import org.web25.http.HttpRequest
import org.web25.http.HttpResponseCallback
import java.util.concurrent.ExecutorService

/**
 * Created by felix on 2/9/16.
 */
class AsynchronousExecutorHttpRequest(private val executorService: ExecutorService) : AsynchronousHttpRequest() {

    override fun execute(callback: HttpResponseCallback?): HttpRequest {
        executorService.submit(getRunnable(callback))
        return this
    }
}
