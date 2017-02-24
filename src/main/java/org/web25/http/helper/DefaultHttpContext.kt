package org.web25.http.helper

import org.web25.http.HttpContext
import org.web25.http.HttpCookieStore
import org.web25.http.HttpResponse
import org.web25.http.auth.Authentication
import org.web25.http.drivers.*
import java.util.*

/**
 * Created by felix on 6/8/16.
 */
class DefaultHttpContext(override val cookieStore : HttpCookieStore) : HttpContext {

    private val authentications = mutableListOf<Authentication>()

    override fun addAuthentication(authentication: Authentication) {
        authentications.add(authentication)
    }

    override fun findAuthentications(httpResponse: HttpResponse): Collection<Authentication> = authentications.filter { it.supports(httpResponse) }

    private val drivers: MutableList<Driver>

    init {
        this.drivers = ArrayList<Driver>()
    }

    override fun base64(): Base64Driver {
        val driver = getFirstDriver(Base64Driver::class.java)
        if (driver != null) {
            return driver
        } else {
            val base64Driver = DefaultBase64Driver()
            useDriver(base64Driver)
            return base64Driver
        }
    }

    private fun <T : Driver> getFirstDriver(type: Class<T>): T? {
        return drivers.filter { type.isAssignableFrom(it.javaClass) }.map { type.cast(it) }.firstOrNull()
    }

    override fun useDriver(driver: Driver) {
        this.drivers.add(driver)
    }

    override fun mime(): MimeService {
        val service = getFirstDriver(MimeService::class.java)
        if (service != null) {
            return service
        } else {
            val mimeService = DefaultMimeService()
            useDriver(mimeService)
            return mimeService
        }
    }
}
