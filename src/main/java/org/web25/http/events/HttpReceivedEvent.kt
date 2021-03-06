package org.web25.http.events

import org.web25.http.HttpRequest
import org.web25.http.HttpResponse

/**
 * Created by felix on 2/11/16.
 */
class HttpReceivedEvent(private val request: HttpRequest, private val response: HttpResponse) : HttpEvent(HttpEventType.RECEIVED) {

    fun request(): HttpRequest {
        return request
    }

    fun response(): HttpResponse {
        return response
    }
}
