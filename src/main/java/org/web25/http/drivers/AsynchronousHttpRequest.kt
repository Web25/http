package org.web25.http.drivers

import org.web25.http.HttpContext
import org.web25.http.HttpResponse
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.client.DefaultHttpRequest

/**
 * Created by felix on 2/9/16.
 */
open class AsynchronousHttpRequest(context : HttpContext) : DefaultHttpRequest(context) {

    override fun execute(callback: ((HttpResponse) -> Unit)?): OutgoingHttpRequest {
        val thread = Thread(getRunnable(callback))
        thread.start()
        return this
    }

    protected fun getRunnable(callback: ((HttpResponse) -> Unit)?): Runnable {
        return Runnable {
            super.execute(callback)
        }
    }
}
