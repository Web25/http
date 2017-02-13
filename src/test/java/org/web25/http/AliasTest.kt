package org.web25.http

import org.web25.http.util.handler

/**
 * Created by Felix Resch on 13-Feb-17.
 */
fun main(args: Array<String>) {
    val http = Http()
    val server = http.server(8080)
    server.get("/", handler { request, response ->
        response.entity("Without value\n")
        true
    }).get("/{value}", handler { request, response ->
        val answer = "With value: "+request.path().substring(1)+"\n";
        response.entity(answer);
        true
    })
    server.start()
}