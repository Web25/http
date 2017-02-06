package org.web25.http.events

/**
 * Created by felix on 2/11/16.
 */
abstract class HttpEvent(private var httpEventType: HttpEventType) {

    fun eventType(): HttpEventType {
        return httpEventType
    }

    fun eventType(httpEventType: HttpEventType) {
        this.httpEventType = httpEventType
    }
}
