package org.web25.http.path

/**
 * Created by felix on 3/2/17.
 */
class HttpPath private constructor(internal var endsWithSlash: Boolean) {

    val map = mutableMapOf<String, String>()

    internal val segments = mutableListOf<PathSegment>()

    private val onPathPrepended = mutableListOf<(HttpPath) -> Unit>()
    private var listening = false


    constructor(path: String): this(path.endsWith("/") && path != "/") {
        populateSegments(path)
    }

    private fun populateSegments(path: String) {
        val parts = splitPath(path)
        parts.forEach {
            if(it.startsWith("{") && it.endsWith("}")) {
                segments.add(DynamicSegment(it.substring(1, it.lastIndex), map, this))
            } else if (it == "*") {
                segments.add(WildcardSegment(this))
            } else {
                segments.add(StaticSegment(it, this))
            }
        }
    }

    operator fun set(name: String, value: String) {
        map[name] = value
    }

    operator fun set(name: String, value: Number) {
        set(name, value.toString())
    }

    operator fun get(name: String) = map[name]

    fun prependPath(path: HttpPath) {
        if(!listening)
            path.onPathPrepended.add {
                listening = true
                prependPath(path)
            }
        segments.addAll(0, path.segments)
        onPathPrepended.forEach { it(path) }
    }

    fun matches(path: String): Boolean {
        val parts = splitPath(path)
        if(parts.size != segments.size)
            return false
        if(endsWithSlash != (path.endsWith("/") && path != "/"))         // TODO remove this to make /api/users/ equal to /api/users
            return false
        segments.forEachIndexed { i, s ->
            if(s is StaticSegment) {
                if(s.render() != parts[i])
                    return false
            } else if (s is DynamicSegment) {
                return true
            }
        }
        return true
    }

    fun match(path: String): HttpPath {
        val httpPath = HttpPath(path.endsWith("/") && path != "/")
        match(path, httpPath)
        return httpPath
    }

    fun match(path: String, httpPath: HttpPath) {
        if(!matches(path)) {
            throw RuntimeException("Invalid path supplied")
        }
        httpPath.prependPath(this)
        val parts = splitPath(path)
        segments.forEachIndexed { i, segment ->
            if(segment is DynamicSegment) {
                httpPath.map[segment.key] = parts[i]
            }
        }
    }

    fun buildActualPath(): String {
        return segments.map(PathSegment::render).joinToString(separator = "/", prefix = "/", postfix = if(endsWithSlash) "/" else "")
    }

    fun buildOriginalPath(): String {
        return segments.map(PathSegment::originalRender).joinToString(separator = "/", prefix = "/", postfix = if(endsWithSlash) "/" else "")
    }

    operator fun invoke(): String = buildActualPath()

    private fun splitPath(path: String): List<String> {
        var finalPath = path
        if(finalPath.startsWith("/")) {
            finalPath = finalPath.substring(1)
        }
        if(finalPath.endsWith("/")) {
            finalPath = finalPath.substring(0, finalPath.lastIndex)
        }
        return if(finalPath == "") listOf() else finalPath.split("/")
    }

    override fun toString() = buildActualPath()

    fun forEach(function: (PathSegment) -> Unit) = segments.forEach(function)

    fun isRoot() = segments.count() == 0
}

