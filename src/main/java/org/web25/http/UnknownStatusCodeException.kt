package org.web25.http

/**
 * Created by felix on 9/10/15.
 */
class UnknownStatusCodeException(status: Int) : RuntimeException("Unknown status " + status)
