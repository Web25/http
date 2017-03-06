package org.web25.http

/**
 * Created by felix on 3/2/17.
 */
class HttpPath private constructor(private val endsWithSlash: Boolean) {

    val map = mutableMapOf<String, String>()

    private val segments = mutableListOf<PathSegment>()


    constructor(path: String): this(path.endsWith("/")) {
        populateSegments(path)
    }

    private fun populateSegments(path: String) {
        val parts = splitPath(path)
        parts.forEach {
            if(it.startsWith("{") && it.endsWith("}")) {
                segments.add(DynamicSegment(it.substring(1, it.lastIndex), map))
            } else {
                segments.add(StaticSegment(it))
            }
        }
    }

    operator fun set(name: String, value: String) {
        map[name] = value
    }

    operator fun get(name: String) = map[name]

    fun prependPath(path: HttpPath) {
        segments.addAll(0, path.segments)
    }

    fun matches(path: String): Boolean {
        val parts = splitPath(path)
        if(parts.size != segments.size)
            return false
        if(endsWithSlash != path.endsWith("/"))         // TODO remove this to make /api/users/ equal to /api/users
            return false
        segments.forEachIndexed { i, s ->
            if(s is StaticSegment) {
                if(s.render() != parts[i])
                    return false
            }
        }
        return true
    }

    fun match(path: String): HttpPath {
        if(!matches(path)) {
            throw RuntimeException("Invalid path supplied")
        }
        val httpPath = HttpPath(endsWithSlash)
        httpPath.prependPath(this)
        val parts = splitPath(path)
        segments.forEachIndexed { i, segment ->
            if(segment is DynamicSegment) {
                httpPath.map[segment.key] = parts[i]
            }
        }
        return httpPath
    }

    fun buildActualPath(): String {
        return segments.map(PathSegment::render).joinToString(separator = "/", prefix = "/", postfix = if(endsWithSlash) "/" else "")
    }

    private fun splitPath(path: String): List<String> {
        var finalPath = path
        if(finalPath.startsWith("/")) {
            finalPath = finalPath.substring(1)
        }
        if(finalPath.endsWith("/")) {
            finalPath = finalPath.substring(0, finalPath.lastIndex)
        }
        return finalPath.split("/")
    }

}

class DynamicSegment(val key: String, val values: MutableMap<String, String>) : PathSegment {
    override fun render(): String = values[key]!!
    override fun toString(): String = "dyn:$$key"
}

class StaticSegment(val segment: String) : PathSegment {
    override fun render(): String = segment.replace("\\{", "{").replace("\\}", "}")
    override fun toString(): String = "static:$segment"
}

interface PathSegment {
    fun render(): String
}
