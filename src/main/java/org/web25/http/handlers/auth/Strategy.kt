package org.web25.http.handlers.auth

import org.web25.http.HttpRequest

/**
 * Created by felix on 6/13/16.
 */
interface Strategy {

    fun authenticate(request: HttpRequest): Boolean

    fun name(): String
    fun realm(): String
    fun authenticateHeader(): String
}