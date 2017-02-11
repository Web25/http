package org.web25.http.exceptions

class CookieNotFoundException(cookie: String) : Exception("Cookie $cookie not found!")