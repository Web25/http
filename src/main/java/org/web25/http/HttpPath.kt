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
        val parts = path.split("/")
        parts.forEach {
            if(it.startsWith("{") && it.startsWith("}")) {
                segments.add(DynamicSegment(it.substring(1, it.lastIndex - 1), map))
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
        val parts = path.split("/")
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
        val parts = path.split("/")
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

}

class DynamicSegment(val key: String, val values: MutableMap<String, String>) : PathSegment {
    override fun render(): String = values[key]!!

}

class StaticSegment(val segment: String) : PathSegment {
    override fun render(): String = segment.replace("\\{", "{").replace("\\}", "}")

}

interface PathSegment {
    fun render(): String
}
