package org.web25.http.path

class DynamicSegment(val key: String, val values: MutableMap<String, String>, httpPath: HttpPath) : PathSegment(httpPath) {
    override fun originalRender(): String {
        return values[key] ?: "{$key}"
    }

    override fun render(): String = values[key]!!
    override fun toString(): String = "dyn:$$key"
}