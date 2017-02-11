package org.web25.http.drivers

import org.web25.http.HttpContext
import org.web25.http.HttpResponse
import org.web25.http.HttpTransport
import org.web25.http.client.HttpResponseCallback
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.client.DefaultHttpRequest
import org.web25.http.exceptions.HttpException
import java.io.IOException
import java.io.PrintStream

/**
 * Created by felix on 2/9/16.
 */
open class AsynchronousHttpRequest(context : HttpContext) : DefaultHttpRequest(context) {

    override fun execute(callback: HttpResponseCallback?): OutgoingHttpRequest {
        val thread = Thread(getRunnable(callback))
        thread.start()
        return this
    }

    protected fun getRunnable(callback: HttpResponseCallback?): Runnable {
        return Runnable {
            val response: HttpResponse?
            try {
                val socket = transport().openSocket(host, port)
                val printStream = PrintStream(socket.outputStream)
                print(printStream)
                response = HttpTransport.def(context).readResponse(socket.inputStream, null)
            } catch (e: IOException) {
                throw HttpException(this@AsynchronousHttpRequest, e)
            }

            callback?.receivedResponse(response)
        }
    }
}
