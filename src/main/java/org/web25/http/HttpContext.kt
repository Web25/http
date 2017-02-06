package org.web25.http

/**
 * Created by felix on 6/8/16.
 */
interface HttpContext {

    fun base64(): Base64Driver

    fun useDriver(driver: Driver)

    fun mime(): MimeService

}
