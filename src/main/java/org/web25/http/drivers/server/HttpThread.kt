package org.web25.http.drivers.server

import org.xjs.dynamic.Pluggable

/**
 * Created by Felix Resch on 29-Apr-16.
 */
class HttpThread : Thread, Pluggable<HttpThread> {

    private var children: MutableList<Any>? = mutableListOf()

    constructor(runnable: Runnable) : super(runnable) {}

    override fun __children(): MutableList<Any> {
        return children!!
    }

    constructor(target: Runnable, children: MutableList<Any>) : super(target) {
        this.children = children
    }

}
