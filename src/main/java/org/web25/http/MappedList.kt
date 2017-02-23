package org.web25.http

open class MappedList<V : Any>(val fieldName: String) {

    private val map = mutableMapOf<String, V>()

    operator fun get(name: String): V = map[name] ?: throw ElementNotFoundException(name, fieldName)

    operator fun set(name: String, cookie: V) {
        map[name] = cookie
    }

    operator fun contains(name: String): Boolean = map.containsKey(name)

    fun forEach(action: (V) -> Unit) {
        map.values.forEach(action)
    }

    fun isNotEmpty() = map.isNotEmpty()

}