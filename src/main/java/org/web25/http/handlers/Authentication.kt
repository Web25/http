package org.web25.http.handlers

import org.jetbrains.annotations.Contract
import org.web25.http.StatusCode
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.handlers.auth.*
import org.web25.http.server.HttpMiddleware
import org.web25.http.server.IncomingHttpRequest
import org.web25.http.server.OutgoingHttpResponse

/**
 * Created by felix on 6/13/16.
 */
class Authentication(private val strategy: Strategy) : HttpMiddleware {

    @Throws(HttpHandleException::class)
    override fun invoke(req: IncomingHttpRequest, res: OutgoingHttpResponse) {
        if (!strategy.authenticate(req)) {
            res.header("WWW-Authenticate", strategy.authenticateHeader())
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
