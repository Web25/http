package org.web25.http.transport.http2

import org.jetbrains.annotations.NonNls
import org.web25.http.Constants

/**
 * Created by felix on 9/16/16.
 */
class Http20Exception : RuntimeException {

    var errorCode = Constants.Http20.ErrorCodes.PROTOCOL_ERROR
        private set

    constructor(@NonNls message: String) : super(message) {}

    constructor(message: String, cause: Throwable) : super(message, cause) {}

    constructor(cause: Throwable) : super(cause) {}

    constructor(@NonNls message: String, errorCode: Int) : super(message) {
        this.errorCode = errorCode
    }

    constructor(message: String, cause: Throwable, errorCode: Int) : super(message, cause) {
        this.errorCode = errorCode
    }

    constructor(cause: Throwable, errorCode: Int) : super(cause) {
        this.errorCode = errorCode
    }
}
