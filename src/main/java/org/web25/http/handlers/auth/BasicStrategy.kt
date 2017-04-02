package org.web25.http.handlers.auth

import org.web25.http.HttpRequest

/**
 * Created by felix on 6/13/16.
 */
class BasicStrategy(realm: String, private val credentialProvider: CredentialProvider) : AbstractStrategy(realm) {

    override fun authenticate(request: HttpRequest): Boolean {
        if (!request.hasHeader("Authorization")) {
            return false
        }
        val base64Driver = request.context.base64()
        val authorization = request.headers["Authorization"]
        if (!authorization.startsWith(name())) {
            return false
        }
        var authData = authorization.substring(authorization.indexOf(" ") + 1)
        authData = String(base64Driver.decodeFromString(authData))
        val username = authData.substring(0, authData.indexOf(":"))
        val password = authData.substring(authData.indexOf(":") + 1)
        val credentials = credentialProvider.findByUsername(username)
        return credentials.password == password
    }

    override fun name(): String {
        return "Basic"
    }
}
