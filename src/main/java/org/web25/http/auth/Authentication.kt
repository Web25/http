package org.web25.http.auth

import org.web25.http.HttpRequest
import org.web25.http.HttpResponse
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.Driver

/**
 * Created by felix on 6/21/16.
 */
interface Authentication : Driver {

    val isInitialized: Boolean
    fun matches(request: HttpRequest): Boolean
    fun supportsMulti(): Boolean
    fun supportsDirect(): Boolean

    fun init(response: HttpResponse)

    fun strategy(): String

    fun authenticate(request: OutgoingHttpRequest)

    fun supports(response: HttpResponse): Boolean {
        val authenticate: String = response.header("WWW-Authenticate").value
        return authenticate.startsWith(strategy())
    }

    companion object {

        fun basic(username: () -> String, password: () -> String): Authentication {
            return DefaultBasicStrategy(username, password)
        }

        fun basic(username: String, password: String): Authentication {
            return DefaultBasicStrategy(username, password)
        }

        fun digest(username: String, password: String): Authentication {
            return DefaultDigestStrategy(username, password)
        }

        fun digest(username: () -> String, password: () -> String): Authentication {
            return DefaultDigestStrategy(username, password)
        }
    }
}
