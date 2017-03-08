package org.web25.http.path

class WildcardSegment(httpPath: HttpPath) : PathSegment(httpPath) {

    override fun render(): String = "*"

    override fun originalRender(): String = "*"

}