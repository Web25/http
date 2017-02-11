package org.web25.http.client

import org.web25.http.HttpResponse

/**
 * Created by felix on 9/10/15.
 */
interface HttpResponseCallback {

    fun receivedResponse(response: HttpResponse)
}
