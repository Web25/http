package org.web25.http.handlers.auth

/**
 * Created by felix on 6/13/16.
 */
abstract class AbstractStrategy protected constructor(private val realm: String) : Strategy {

    override fun realm(): String {
        return realm
    }

    override fun authenticateHeader(): String {
        return String.format("%s realm=%s", name(), realm())
    }
}
