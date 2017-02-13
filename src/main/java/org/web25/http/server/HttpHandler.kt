package org.web25.http.server


/**
 * Created by Felix Resch on 25-Apr-16.
 */
@FunctionalInterface
interface HttpHandler: HttpExecutable<Boolean>
