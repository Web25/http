package org.web25.http.helper

/**
 * Created by felix on 6/4/16.
 */
class HttpSocketOptions {

    var isClose: Boolean = false
    private var handledCallback: HandledCallback? = null

    fun hasHandledCallback(): Boolean {
        return handledCallback != null
    }

    fun callHandledCallback() {
        handledCallback!!.sent()
    }

    fun setHandledCallback(handledCallback: HandledCallback) {
        this.handledCallback = handledCallback
    }
}
