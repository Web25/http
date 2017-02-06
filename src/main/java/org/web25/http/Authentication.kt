package org.web25.http

import org.jetbrains.annotations.Contract
import org.web25.http.auth.DefaultBasicStrategy
import org.web25.http.auth.DefaultDigestStrategy
import java.util.function.Supplier

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

    fun authenticate(request: HttpRequest)

    fun supports(response: HttpResponse): Boolean {
        val authenticate: String = response.header("WWW-Authenticate")!!.value
        return authenticate.startsWith(strategy())
    }

    companion object {

        @Contract("_, _ -> !null")
        fun basic(username: Supplier<String>, password: Supplier<String>): Authentication {
            return DefaultBasicStrategy(username, password)
        }

        @Contract("_, _ -> !null")
        fun basic(username: String, password: String): Authentication {
            return DefaultBasicStrategy(username, password)
        }

        @Contract("_, _ -> !null")
        fun digest(username: String, password: String): Authentication {
            return DefaultDigestStrategy(username, password)
        }

        @Contract("_, _ -> !null")
        fun digest(username: Supplier<String>, password: Supplier<String>): Authentication {
            return DefaultDigestStrategy(username, password)
        }
    }
}
