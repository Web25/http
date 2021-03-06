package org.web25.http.util

/**
 * Copied and modified from https://github.com/gazolla/Kotlin-Algorithm/tree/master/Stack
 */
class Stack<T>(list:MutableList<T> = mutableListOf()) {

    var items: MutableList<T> = list


    fun isEmpty():Boolean = this.items.isEmpty()

    fun count():Int = this.items.count()

    fun push(element:T) {
        val position = this.count()
        this.items.add(position, element)
    }

    override  fun toString() = this.items.toString()

    fun pop():T? {
        if (this.isEmpty()) {
            return null
        } else {
            val item =  this.items.count() - 1
            return this.items.removeAt(item)
        }
    }

    fun peek():T? {
        if (isEmpty()) {
            return null
        } else {
            return this.items[this.items.count() - 1]
        }
    }

    fun popEach(action: (T) -> Unit) {
        while (!isEmpty()) {
            val element = pop()
            if(element != null)
                action(element)
        }
    }

}