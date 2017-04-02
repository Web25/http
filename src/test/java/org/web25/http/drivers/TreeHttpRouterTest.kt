package org.web25.http.drivers

import org.junit.jupiter.api.Test
import org.web25.http.util.handler
import org.web25.http.util.middleware
import org.web25.http.util.nop

/**
 * Created by felix on 3/7/17.
 */
class TreeHttpRouterTest {
    @Test
    fun testTree() {
        val router = TreeHttpRouter()
        router.get("/hello", handler { _, response -> response.entity("Hello World"); true })
        router.get("/hello/world", handler { _, response -> response.entity("Another Hello World"); true })
        router.get("/", handler { _, response -> response.entity("Index"); true })
        router.use(middleware { request, _ ->
            println(request.requestLine())
        })
        val externalRouter = TreeHttpRouter()
        externalRouter.get("/", handler { _, response -> response.entity("External stuff"); true })
        externalRouter.get("/hello/", handler { _, response -> response.entity("Another hello from ext"); true })
        router.use("/ext/", externalRouter)
        router.nop()
    }
}