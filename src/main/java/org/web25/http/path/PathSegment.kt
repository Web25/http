package org.web25.http.path

abstract class PathSegment(val path: HttpPath) {
    abstract fun render(): String
    abstract fun originalRender(): String
}