package org.web25.http.transport.http2

import org.web25.http.Constants

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by felix on 9/4/16.
 */
class FlowControlWindow(private val connection: HttpConnection, private val parent: FlowControlWindow? = null) {

    private val local: AtomicInteger
    private val remote: AtomicInteger

    init {
        this.local = AtomicInteger(connection.localSettings.initialWindowSize)
        this.remote = AtomicInteger(connection.remoteSettings.initialWindowSize)

    }

    fun checkIncoming(length: Int): Boolean {
        if (parent != null) {
            if (!parent.checkIncoming(length)) {
                return false
            }
        }
        return local.get() >= length
    }

    fun checkOutgoing(length: Int): Boolean {
        if (parent != null) {
            if (!parent.checkOutgoing(length)) {
                return false
            }
        }
        return remote.get() >= length
    }

    fun decreaseLocal(length: Int) {
        if (!checkIncoming(length))
            throw HttpFlowControlException("The local flow control window is too small to support the incoming DATA frame!")
        parent?.decreaseLocal(length)
        local.updateAndGet { i -> i - length }
    }

    fun decreaseRemote(length: Int) {
        if (!checkOutgoing(length))
            throw HttpFlowControlException("The remote flow control window is too small to support the outgoing DATA frame")
        parent?.decreaseRemote(length)
        remote.updateAndGet { i -> i - length }
    }

    fun incrementLocal(length: Int) {
        if (local.get() + length < 0)
            throw Http20Exception("Invalid size for flow control window", Constants.Http20.ErrorCodes.FLOW_CONTROL_ERROR)
        local.updateAndGet { i -> i + length }
    }

    fun incremetRemote(length: Int) {
        if (local.get() + length < 0)
            throw Http20Exception("Invalid size for flow control window", Constants.Http20.ErrorCodes.FLOW_CONTROL_ERROR)
        remote.updateAndGet { i -> i + length }
    }
}
