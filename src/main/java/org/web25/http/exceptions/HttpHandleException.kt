package org.web25.http.exceptions

import org.web25.http.StatusCode

/**
 * Created by felix on 4/25/16.
 */
class HttpHandleException : Exception {

    var statusCode: StatusCode? = null
        private set

    constructor(statusCode: StatusCode) : super() {
        this.statusCode = statusCode
    }

    constructor(statusCode: StatusCode, message: String) : super(message) {
        this.statusCode = statusCode
    }

    constructor(statusCode: StatusCode, s: String, throwable: Throwable) : super(s, throwable) {
        this.statusCode = statusCode
    }

    constructor() {
        this.statusCode = StatusCode.INTERNAL_SERVER_ERROR
    }

    constructor(s: String) : super(s) {
        this.statusCode = StatusCode.INTERNAL_SERVER_ERROR
    }

    constructor(s: String, throwable: Throwable) : super(s, throwable) {
        this.statusCode = StatusCode.INTERNAL_SERVER_ERROR
    }
}
