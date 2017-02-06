package org.web25.http.drivers

import org.web25.http.*
import java.io.IOException
import java.io.PrintStream

/**
 * Created by felix on 2/9/16.
 */
open class AsynchronousHttpRequest : DefaultHttpRequest() {

    override fun execute(callback: HttpResponseCallback?): HttpRequest {
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
                response = HttpTransport.def().readResponse(socket.inputStream, null)
            } catch (e: IOException) {
                throw HttpException(this@AsynchronousHttpRequest, e)
            }

            callback?.receivedResponse(response)
        }
    }
}
