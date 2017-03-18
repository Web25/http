package org.web25.http.entities

import org.web25.http.HttpEntity

/**
 * Simple class that creates a string representation of an entity
 */
abstract class AbstractStringableEntity: HttpEntity {

    override fun toString(): String {
        return String(getBytes())
    }
}