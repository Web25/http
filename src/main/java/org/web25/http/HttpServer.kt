package org.web25.http

/**
 * Created by Felix Resch on 25-Apr-16.
 */
interface HttpServer : HttpRoutable<HttpServer> {

    fun start(): HttpServer
    fun stop(): HttpServer
    fun secure(): HttpsServer

    fun ready(): Boolean
}
