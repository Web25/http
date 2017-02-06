package org.web25.http

/**
 * Created by felix on 9/10/15.
 */
class HttpHeader(val name: String, val value: String): Comparable<String> {


    override fun compareTo(other: String): Int {
        return value.compareTo(other)
    }

    fun asInt(): Int {
        return Integer.parseInt(value)
    }

    override fun equals(other: Any?): Boolean {
        if(other == null)
            return false
        if(other is String)
            return other == value
        if (other is HttpHeader) {
            if (this === other) {
                return true
            } else if (this.name == other.name && this.value == other.value) {
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
