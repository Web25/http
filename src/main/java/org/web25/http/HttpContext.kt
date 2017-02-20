package org.web25.http

import org.web25.http.auth.Authentication
import org.web25.http.drivers.Base64Driver
import org.web25.http.drivers.Driver
import org.web25.http.drivers.MimeService

/**
 * Created by felix on 6/8/16.
 */
interface HttpContext {

    fun base64(): Base64Driver

    fun useDriver(driver: Driver)

    fun mime(): MimeService

    fun addAuthentication(authentication: Authentication)
    fun findAuthentications(httpResponse: HttpResponse): Collection<Authentication>

    val cookieStore: HttpCookieStore

}
