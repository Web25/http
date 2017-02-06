package org.web25.http.handlers.auth

import org.web25.http.HttpRequest
import org.web25.http.helper.HttpHelper

/**
 * Created by felix on 6/13/16.
 */
class BasicStrategy(realm: String, private val credentialProvider: CredentialProvider) : AbstractStrategy(realm) {

    override fun authenticate(request: HttpRequest): Boolean {
        if (!request.hasHeader("Authorization")) {
            return false
        }
        val base64Driver = HttpHelper.context().base64()
        val authorization = request.header("Authorization").value
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
