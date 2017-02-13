package org.web25.http.exceptions

class HeaderNotFoundException(header: String) : Exception("Header $header not found!")