package org.web25.http

import org.web25.http.drivers.AsynchronousDriver
import org.web25.http.drivers.DefaultDriver

/**
 * Created by felix on 2/9/16.
 */
object HttpDrivers {

    fun defaultDriver(): HttpDriver {
        return DefaultDriver()
    }

    fun asyncDriver(): HttpDriver {
        return AsynchronousDriver()
    }

    fun asyncDriver(threads: Int): HttpDriver {
        return AsynchronousDriver(threads)
    }

}
