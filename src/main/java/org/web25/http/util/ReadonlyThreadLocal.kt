package org.web25.http.util

/**
 * Created by felix on 6/9/16.
 */
open class ReadonlyThreadLocal<T> : ThreadLocal<T>() {

    override fun set(t: T) {}

    override fun remove() {}
}
