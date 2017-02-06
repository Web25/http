package org.web25.http

/**
 * Created by felix on 9/14/16.
 */
interface HttpsServer : HttpServer {

    fun keystorePass(keystorePass: String): HttpsServer
    fun keyPass(keyPass: String): HttpsServer
    fun keystore(keystore: String): HttpsServer
}
