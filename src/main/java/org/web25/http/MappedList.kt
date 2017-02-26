package org.web25.http

open class MappedList<V : Any>(val fieldName: String, val stringOperator: (String) -> String = {it}) {

    private val map = mutableMapOf<String, V>()

    operator fun get(name: String): V = map[stringOperator(name)] ?: throw ElementNotFoundException(name, fieldName)

    operator fun set(name: String, cookie: V) {
        map[stringOperator(name)] = cookie
    }

    operator fun contains(name: String): Boolean = map.containsKey(stringOperator(name))

    fun forEach(action: (V) -> Unit) {
        map.values.forEach(action)
    }

    fun forEach(action: (String, V) -> Unit) {
        map.forEach { action(it.key, it.value) }
    }

    fun isNotEmpty() = map.isNotEmpty()

}

