package org.web25.http

import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.InMemoryCookieStore

/**
 * Interface for classes that store cookies. Default Implementation for this class is DefaultHttpCookieStore.
 *
 * @since 0.2.0
 * @author Felix Resch <felix.resch@web25.org>
 */
interface HttpCookieStore {

    /**
     * Stores a cookie in the cookie store. Implementations should take note of the storage requirements defined in
     * RFC 6265
     *
     * @param request The request that has been sent to the server
     * @param response The response that has been received
     */
    fun store(request: OutgoingHttpRequest, response: HttpResponse)

    /**
     * Looks through all cookies in the store and adds the cookies matching the request to the request
     *
     * @param request The request that should be sent
     */
    fun findCookies(request: OutgoingHttpRequest)

    /**
     * Lists all cookies currently stored in the cookie store
     *
     * @return all cookies currently stored
     */
    fun allCookies(): List<HttpCookie>

    /**
     * Removes cookies from the cookie store that have are no longer up to date.
     *
     * This method should be called before every reading access
     */
    fun purge()

    /**
     * Removes all cookies from the cookie store.
     */
    fun clear()

    /**
     * Closes the current instance of the cookie store. If the cookie store supports persistence this function should
     * flush and clear up the store.
     */
    fun close()

    companion object {
        fun default() = InMemoryCookieStore()
    }
}