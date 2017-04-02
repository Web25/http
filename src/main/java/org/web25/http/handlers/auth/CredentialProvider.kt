package org.web25.http.handlers.auth

/**
 * Created by felix on 6/13/16.
 */
interface CredentialProvider {

    fun findByUsername(username: String): Credentials

    class Credentials {

        var username: String? = null
        var password: String? = null

        constructor() {}

        constructor(username: String, password: String) {
            this.username = username
            this.password = password
        }
    }
}
