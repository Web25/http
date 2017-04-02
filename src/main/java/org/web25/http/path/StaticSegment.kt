package org.web25.http.path

class StaticSegment(val segment: String, httpPath: HttpPath) : PathSegment(httpPath) {
    override fun originalRender(): String = render()

    override fun render(): String = segment.replace("\\{", "{").replace("\\}", "}")
    override fun toString(): String = "static:$segment"
}