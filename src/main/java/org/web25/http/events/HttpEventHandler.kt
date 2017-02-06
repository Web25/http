package org.web25.http.events

/**
 * Created by felix on 2/11/16.
 */
interface HttpEventHandler {

    fun handle(event: HttpEvent)
}
