package org.web25.http.drivers

import org.web25.http.HttpCookie
import org.web25.http.HttpCookieStore
import org.web25.http.HttpResponse
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.util.HttpCookieHelper
import java.time.ZonedDateTime

/**
 * In memory implementation of a cookie store. This cookie store does not support persistence and will lose all cookies
 * after a restart.
 *
 * @since 0.2.0
 * @author Felix Resch <felix.resch@web25.org>
 */
class InMemoryCookieStore: HttpCookieStore {

    private val cookies: MutableList<Cookie> = mutableListOf()

    override fun store(request: OutgoingHttpRequest, response: HttpResponse) {
        synchronized(cookies, {
            response.cookies.values.forEach {
                val persistent: Boolean
                val expiryDate: ZonedDateTime
                val maxAge = it.maxAge
                val expires = it.expires
                if(maxAge != null) {
                    persistent = true
                    expiryDate = ZonedDateTime.now().plusDays(maxAge)
                } else if (expires != null) {
                    persistent = true
                    expiryDate = expires
                } else {
                    persistent = false
                    expiryDate = ZonedDateTime.now().plusYears(10000)
                }
                val domain = it.domain ?: ""
                val hostOnly: Boolean
                val cookieDomain: String
                //TODO add public suffix checking
                if(domain.isEmpty()) {
                    hostOnly = true
                    cookieDomain = HttpCookieHelper.canonicalizeHostname(request.host())
                } else {
                    if(!HttpCookieHelper.matchDomain(HttpCookieHelper.canonicalizeHostname(domain), HttpCookieHelper.canonicalizeHostname(request.host())))
                        return@forEach
                    hostOnly = false
                    cookieDomain = HttpCookieHelper.canonicalizeHostname(domain)
                }
                val path = it.path ?: HttpCookieHelper.defaultCookiePath(request.path())
                val name = it.name
                val oldCookie = cookies.filter { it.domain == cookieDomain && it.path == path && it.name == name }
                val creationTime = if (oldCookie.count() > 0) oldCookie[0].creationTime else ZonedDateTime.now()
                cookies.removeAll { it.domain == cookieDomain && it.path == path && it.name == name }
                cookies.add(Cookie(name, it.value, expiryDate, cookieDomain, path, creationTime, ZonedDateTime.now(), persistent, hostOnly, it.httpOnly, it.secure))
            }
        })
    }

    override fun findCookies(request: OutgoingHttpRequest) {
        synchronized(cookies, {
            cookies.filter {
                if(it.hostOnlyFlag) {
                    HttpCookieHelper.canonicalizeHostname(request.host()) == it.domain
                } else {
                    HttpCookieHelper.matchDomain(it.domain, HttpCookieHelper.canonicalizeHostname(request.host()))
                }
            }.filter {
                HttpCookieHelper.matchPath(it.path, request.path())
            }.filter {
                //TODO add filtering for secure flag
                true
            }.sortedBy { it.path.length }
            .forEach {
                it.lastAccessTime = ZonedDateTime.now()
                request.cookie(it.name, it.value)
            }
        })
    }

    override fun allCookies(): List<HttpCookie> = TODO()

    override fun purge() {
        synchronized(cookies, {
            val now = ZonedDateTime.now()
            cookies.removeAll {
                it.expiryTime.isBefore(now)
            }
        })
    }

    override fun clear() {
        synchronized(cookies, {
            cookies.clear()
        })
    }

    override fun close() {
        //nothing to do here
    }

    private data class Cookie(val name: String, val value: String, val expiryTime: ZonedDateTime,
                              val domain: String, val path: String, val creationTime: ZonedDateTime,
                              var lastAccessTime: ZonedDateTime, val persistentFlag: Boolean,
                              val hostOnlyFlag: Boolean, val httpOnlyFlag: Boolean, val secureFlag: Boolean)
}