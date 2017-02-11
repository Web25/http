package org.web25.http

import org.web25.http.drivers.AsynchronousDriver
import org.web25.http.drivers.DefaultDriver

/**
 * Created by felix on 2/9/16.
 */
object HttpDrivers {

    fun defaultDriver(context : HttpContext): HttpDriver {
        return DefaultDriver(context)
    }

    fun asyncDriver(context : HttpContext): HttpDriver {
        return AsynchronousDriver(context)
    }

    fun asyncDriver(threads: Int, context : HttpContext): HttpDriver {
        return AsynchronousDriver(threads, context)
    }

}
