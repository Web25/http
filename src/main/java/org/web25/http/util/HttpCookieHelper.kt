package org.web25.http.util

import org.slf4j.LoggerFactory
import org.web25.http.HttpCookie
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by felix on 2/18/17.
 */
object HttpCookieHelper {

    private val log = LoggerFactory.getLogger("HTTP")

    /**
     * As defined in RFC 6265, Section 5.2. The Set-Cookie Header
     *
     * Parses a cookie from a cookie header string. It does not set the default path of the cookie or the max-age expiry date.
     * These tasks are left to be done by the cookie store.
     *
     * @param cookieString the value of the cookie header that should be parsed
     * @return the parsed cookie as HttpCookie
     * @since 0.2.0
     * @author Felix Resch <felix.resch@web25.org>
     */
    fun readCookie(cookieString: String): HttpCookie {
        val nameValuePair: String
        val hasAttributes = cookieString.contains(';')
        if (hasAttributes) {
            nameValuePair = cookieString.substring(0, cookieString.indexOf(';'))
        } else {
            nameValuePair = cookieString
        }
        if (!nameValuePair.contains('='))
            throw HttpCookieException("Invalid Cookie. Cookie String does not contain \"=\"")
        val name = nameValuePair.substring(0, nameValuePair.indexOf('=')).trim()
        val value = nameValuePair.substring(nameValuePair.indexOf('=') + 1).trim()
        if (name.isEmpty())
            throw HttpCookieException("Invalid Cookie. Cookie name is empty")
        val cookie = HttpCookie(name, value)
        if (hasAttributes) {
            val unparsedAttributes = cookieString.substring(cookieString.indexOf(';') + 1)
            unparsedAttributes.split(";").forEach {
                val attributeName: String
                val attributeValue: String
                if(it.contains('=')) {
                    attributeName = it.substring(0, it.indexOf('=')).trim()
                    attributeValue = it.substring(it.indexOf('=') + 1).trim()
                } else {
                    attributeName = it.trim()
                    attributeValue = ""
                }
                when {
                    attributeName.equals(other = "expires", ignoreCase = true) -> {
                        if(attributeValue.isEmpty())
                            throw HttpCookieException("Invalid Cookie. Expires attribute is empty")
                        //val unzoned = ZonedDateTime.parse(attributeValue, DateTimeFormatter.RFC_1123_DATE_TIME)
                        val dateTime = ZonedDateTime.parse(attributeValue, DateTimeFormatter.RFC_1123_DATE_TIME)
                        cookie.expires = dateTime
                    }
                    attributeName.equals(other = "max-age", ignoreCase = true) -> {
                        if(attributeValue.isEmpty())
                            throw HttpCookieException("Invalid Cookie. Max-Age attribute is empty")
                        cookie.maxAge = attributeValue.toDouble().toLong()
                    }
                    attributeName.equals(other = "domain", ignoreCase = true) -> {
                        if(attributeValue.isEmpty())
                            throw HttpCookieException("Invalid Cookie. Domain attribute is empty")
                        cookie.domain = (if(attributeValue.startsWith(".")) attributeValue.substring(1) else attributeValue).toLowerCase()
                    }
                    attributeName.equals(other = "path", ignoreCase = true) -> {
                        if(!(attributeValue.isEmpty() || attributeValue == "/")) {
                            cookie.path = attributeValue
                        }
                    }
                    attributeName.equals(other = "secure", ignoreCase = true) -> {
                        cookie.secure = true
                    }
                    attributeName.equals(other = "httponly", ignoreCase = true) -> {
                        cookie.httpOnly = true
                    }
                }
            }
        }
        return cookie
    }

    fun canonicalizeHostname(hostname: String): String {
        var result = hostname.toLowerCase()
        result = result.replace('\u3002', '.')
        return result
    }

    /**
     * As defined in RFC 6265, Section 5.1.3. Domain Matching
     *
     * Checks if string matches domainstring
     *
     * @param domainString The domain the string should be matched against
     * @param string The string that should be matched
     * @return if the given string matches the domain
     * @since 0.2.0
     * @author Felix Resch <felix.resch@web25.org>
     */
    fun matchDomain(domainString: String, string: String): Boolean {
        val canonicalizedDomainString = canonicalizeHostname(domainString)
        val canonicalizedString = canonicalizeHostname(string)
        return canonicalizedDomainString == canonicalizedString || canonicalizedDomainString.endsWith(canonicalizedString)
    }

    /**
     * As defined in RFC 6265, Section 5.1.4. Paths and Path-Match
     *
     * Creates a default path for cookies received in response to a certain http request
     *
     * @param uriPath The path of the HTTP request that has been issued to the server
     * @return the default cookie path of the cookie
     * @since 0.2.0
     * @author Felix Resch <felix.resch@web25.org>
     */
    fun defaultCookiePath(uriPath: String): String {
        if(uriPath.isEmpty() || !uriPath.startsWith("/"))
            return "/"
        if(uriPath.occurrences('/') == 1)
            return "/"
        return uriPath.substring(0, uriPath.lastIndexOf('/'))
    }

    /**
     * As defined in RFC 6265, Section 5.1.4 Paths and Path-Match
     *
     * Checks if a cookiePath matches a requestPath
     *
     * @param cookiePath the path of the cookie that has been stored
     * @param requestPath the path of the request
     * @return whether the request matches the cookie or not
     * @author Felix Resch <felix.resch@web25.org>
     * @since 0.2.0
     */
    fun matchPath(cookiePath: String, requestPath: String): Boolean {
        val equals = cookiePath == requestPath
        if(equals)
            return true
        val isSubpath = (cookiePath.endsWith("/") && requestPath.startsWith(cookiePath))
        if(isSubpath)
            return true
        val otherThing = (requestPath.startsWith(cookiePath) && requestPath[cookiePath.length] == '/')
        if(otherThing)
            return true
        return false
    }

    /**
     * Reads cookies from an incoming cookie header.
     *
     * Note: All invalid cookies will be dropped.
     *
     * @param cookieString the value of the cookie header
     * @return a list of the valid cookies in the cookieString
     * @author Felix Resch <felix.resch@web25.org>
     * @since 0.2.0
     */
    fun readCookies(cookieString : String): List<HttpCookie> {
        if(cookieString.contains(";")) {
            val cookieList = mutableListOf<HttpCookie>()
            cookieString.split(";").forEach {
                try {
                    cookieList.add(readServerCookie(it))
                } catch (e: HttpCookieException) {
                    log.warn("Parsing of cookie failed", e)
                }
            }
            return cookieList
        } else {
            return listOf(readServerCookie(cookieString))
        }
    }

    /**
     * Helper function for readCookies
     */
    private fun readServerCookie(cookieString : String): HttpCookie {
        if(!cookieString.contains("="))
            throw HttpCookieException("Invalid Cookie. Cookies String does not contain `=`")
        val name = cookieString.substring(0, cookieString.indexOf("=")).trim()
        val value = cookieString.substring(cookieString.indexOf("=") + 1).trim()
        return HttpCookie(name, value)
    }
}

