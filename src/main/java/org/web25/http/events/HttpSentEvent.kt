package org.web25.http.events

import org.web25.http.HttpRequest

/**
 * Created by felix on 2/11/16.
 */
class HttpSentEvent(private val request: HttpRequest) : HttpEvent(HttpEventType.SENT) {

    fun request(): HttpRequest {
        return request
    }
}
