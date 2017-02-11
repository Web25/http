package org.web25.http.exceptions

/**
 * Created by felix on 9/10/15.
 */
class UnknownStatusCodeException(status: Int) : RuntimeException("Unknown status " + status)
