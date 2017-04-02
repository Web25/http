package org.web25.http.auth2

import org.web25.http.HttpResponse
import org.web25.http.client.OutgoingHttpRequest

/**
 * Base interface for all Http authentication related classes
 *
 * @author Felix Resch <felix.resch@web25.org>
 * @since 0.0.2
 */
interface Authentication {

    /**
     * Function that generates the required headers to authenticate the request
     *
     * @param response the response to an unauthorized request sent to the server
     * @param request the request that should be sent to the server
     */
    fun authenticate(response: HttpResponse, request: OutgoingHttpRequest)

    /**
     * Checks if this authentication instance can provide credentials for the received request
     *
     * @param request the response to an unauthorized request sent to the server
     * @return true - if this authentication can provide authentication for this request
     */
    fun matchesAuthentication(request: OutgoingHttpRequest): Boolean

    /**
     * The name of the realm this Authentication can authenticate requests for
     */
    val realm: String

    /**
     * The hostname of the server (including port) this authentication instance can authenticate requests for
     */
    val host: String

    companion object {

        /**
         * Creates a Authentication instance with the given functions and values
         *
         * @param authenticator The authenticator used for this instance of Authentication
         * @param authenticationMatcher The matcher for this instance of Authentication
         * @param realm The realm of the created authentication
         * @param host The host of the created authentication
         * @return a authentication using the given parameters
         */
        fun of(authenticator: Authenticator, authenticationMatcher: AuthenticationMatcher,
               realm: String, host: String): Authentication {
            return object : Authentication {
                override fun authenticate(response: HttpResponse, request: OutgoingHttpRequest) = authenticator(response, request)

                override fun matchesAuthentication(request: OutgoingHttpRequest): Boolean = authenticationMatcher(request)

                override val realm: String = realm
                override val host: String = host

            }
        }
    }
}

typealias Authenticator = (response: HttpResponse, request: OutgoingHttpRequest) -> Unit
typealias AuthenticationMatcher = (request: OutgoingHttpRequest) -> Boolean