package org.web25.http.server.config

import org.web25.http.server.Configurator
import java.io.InputStream
import java.util.*

/**
 * Created by Felix Resch on 14-Feb-17.
 */
abstract class PropertiesConfigurator(protected val inputStream: InputStream): Configurator {

    private val properties by lazy {
        val properties = Properties()
        properties.load(inputStream)
        properties
    }

    override fun getInt(name: String): Int = properties.getProperty(name).toInt()

    override fun getString(name: String): String = properties.getProperty(name)

    override fun getBoolean(name: String): Boolean {
        val prop = properties.getProperty(name)
        return prop == "true" || prop == "1"
    }
}