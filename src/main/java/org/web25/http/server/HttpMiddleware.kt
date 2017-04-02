package org.web25.http.server

/**
 * Created by felix on 4/26/16.
 */
@FunctionalInterface
interface HttpMiddleware: HttpExecutable<Unit>
