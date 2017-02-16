package org.web25.http.server

import org.web25.http.server.config.FileConfigurator
import org.web25.http.server.config.ResourceConfigurator
import java.io.File

/**
 * Created by Felix Resch on 14-Feb-17.
 */
interface Configurator {

    fun getInt(name: String): Int
    fun getString(name: String): String
    fun getBoolean(name: String): Boolean

    companion object {
        infix fun file(file: File): Configurator = FileConfigurator(file)

        infix fun file(file: String): Configurator = this file File(file)

        infix fun resource(name: String): Configurator = ResourceConfigurator(name)
    }
}

