package org.web25.http.drivers.server

/**
 * Created by felix on 9/19/16.
 */
interface ExecutorListener {

    fun submit(runnable: Runnable)
}
