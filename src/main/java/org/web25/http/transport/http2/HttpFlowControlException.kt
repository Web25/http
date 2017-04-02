package org.web25.http.transport.http2

import org.jetbrains.annotations.NonNls

/**
 * Created by felix on 9/4/16.
 */
class HttpFlowControlException : RuntimeException {

    constructor(@NonNls message: String) : super(message) {}

    constructor(message: String, cause: Throwable) : super(message, cause) {}

    constructor(cause: Throwable) : super(cause) {}
}
