package org.web25.http.exceptions

import org.web25.http.HttpRequest

/**
 * Created by felix on 1/19/16.
 */
class HttpException : RuntimeException {

    private var request: HttpRequest? = null

    constructor(request: HttpRequest, cause: Exception) : super(cause) {
        this.request = request
    }

    constructor(request: HttpRequest, message: String) : super(message) {
        this.request = request
    }

}
