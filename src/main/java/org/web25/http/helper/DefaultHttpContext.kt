package org.web25.http.helper

import org.web25.http.Base64Driver
import org.web25.http.Driver
import org.web25.http.HttpContext
import org.web25.http.MimeService
import org.web25.http.drivers.DefaultBase64Driver
import org.web25.http.drivers.DefaultMimeService
import java.util.*

/**
 * Created by felix on 6/8/16.
 */
class DefaultHttpContext : HttpContext {

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
