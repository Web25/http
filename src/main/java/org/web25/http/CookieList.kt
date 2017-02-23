package org.web25.http

class CookieList: MappedList<HttpCookie>("cookies") {

    operator fun set(name: String, value: String) {
        this[name] = HttpCookie(name, value)
    }

    infix fun add(cookie : HttpCookie) {
        this[cookie.name] = cookie
    }

}