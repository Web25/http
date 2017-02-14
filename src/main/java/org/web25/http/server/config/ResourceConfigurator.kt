package org.web25.http.server.config

/**
 * Created by Felix Resch on 14-Feb-17.
 */
class ResourceConfigurator(name: String) : PropertiesConfigurator(ResourceConfigurator::class.java.classLoader.getResourceAsStream(name))