package org.web25.http

/**
 * Created by felix on 9/10/15.
 */
interface HttpResponseCallback {

    fun receivedResponse(response: HttpResponse)
}
