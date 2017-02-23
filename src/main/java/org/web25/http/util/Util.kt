package org.web25.http.util

/**
 * Created by felix on 2/18/17.
 */

fun String.occurrences(s: Char): Int {
    var counter = 0
    forEach {
        if(it == s)
            counter++
    }
    return counter
}

fun Any.nop() {

}