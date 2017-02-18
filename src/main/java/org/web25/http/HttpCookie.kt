package org.web25.http

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by felix on 9/10/15.
 */
data class HttpCookie(var name: String, var value: String, var expires: ZonedDateTime? = null, var maxAge: Long? = null, var domain: String? = null, var path: String? = null,
                      var secure: Boolean = false, var httpOnly: Boolean = false) {

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(name)
                .append("=")
                .append(value)
        val maxAge = this.maxAge
        val expires = this.expires
        val domain = this.domain
        val path = this.path
        if(maxAge != null) {
            builder.append("; Max-Age=")
                    .append(maxAge)
        } else if (expires != null) {
            builder.append("; Expires=")
                    .append(expires.format(DateTimeFormatter.RFC_1123_DATE_TIME))
        }
        if(domain != null) {
            builder.append("; Domain=")
                    .append(domain)
        }
        if(path != null) {
            builder.append("; Path=")
                    .append(path)
        }
        if(secure) {
            builder.append("; Secure")
        }
        if(httpOnly) {
            builder.append("; HttpOnly")
        }
        return builder.toString()
    }
}
