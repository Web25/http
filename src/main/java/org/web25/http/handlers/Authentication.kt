package org.web25.http.handlers

import org.jetbrains.annotations.Contract
import org.web25.http.*
import org.web25.http.handlers.auth.*

/**
 * Created by felix on 6/13/16.
 */
class Authentication(private val strategy: Strategy) : HttpMiddleware {

    @Throws(HttpHandleException::class)
    override fun handle(request: HttpRequest, response: HttpResponse) {
        if (!strategy.authenticate(request)) {
            response.header("WWW-Authenticate", strategy.authenticateHeader())
            throw HttpHandleException(StatusCode.UNAUTHORIZED, "The requested resource has restricted access!")
        }
    }

    companion object {

        @Contract("_, _ -> !null")
        fun basic(realm: String, credentialProvider: CredentialProvider): Authentication {
            return Authentication(BasicStrategy(realm, credentialProvider))
        }

        @Contract("_, _ -> !null")
        fun digest(realm: String, credentialProvider: CredentialProvider): Authentication {
            return Authentication(DigestStrategy(realm, credentialProvider, SimpleNonceManager()))
        }

        @Contract("_, _, _ -> !null")
        fun digest(realm: String, credentialProvider: CredentialProvider, nonceManager: NonceManager): Authentication {
            return Authentication(DigestStrategy(realm, credentialProvider, nonceManager))
        }
    }
}
