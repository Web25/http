package org.web25.http.path

/**
 * Created by felix on 3/2/17.
 */
class HttpPath private constructor(internal var endsWithSlash: Boolean) {

    val map = mutableMapOf<String, String>()

    internal val segments = mutableListOf<PathSegment>()

    private val onPathPrepended = mutableListOf<(HttpPath) -> Unit>()
    private var listening = false

    val query: MutableMap<String, Any> = mutableMapOf()

    constructor(path: String): this((path.endsWith("/") || (path.contains("?") && path[path.indexOf("?") - 1] == '/')) && path != "/") {
        populateSegments(path)
    }

    private fun populateSegments(path: String) {
        val (parts, query) = splitPath(path)
        parts.forEach {
            if(it.startsWith("{") && it.endsWith("}")) {
                segments.add(DynamicSegment(it.substring(1, it.lastIndex), map, this))
            } else if (it == "*") {
                segments.add(WildcardSegment(this))
            } else {
                segments.add(StaticSegment(it, this))
            }
        }
        query.forEach {
            val name = with(it) { substring(0, indexOf('=')) }
            val value = with(it) { substring(indexOf('=') + 1) }
            this.query[name] = value
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
        val (parts, _) = splitPath(path)
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
        val (parts, _) = splitPath(path)
        segments.forEachIndexed { i, segment ->
            if(segment is DynamicSegment) {
                httpPath.map[segment.key] = parts[i]
            }
        }
    }

    fun requestPath(): String {
        return segments.map(PathSegment::render).joinToString(separator = "/", prefix = "/", postfix = if(endsWithSlash) "/" else "") +
                if(query.isNotEmpty()) {
                    query.map {
                        it.key + "=" + it.value
                    }.joinToString(separator = "&", prefix = "?")
                } else ""
    }

    fun buildOriginalPath(): String {
        return segments.map(PathSegment::originalRender).joinToString(separator = "/", prefix = "/", postfix = if(endsWithSlash) "/" else "")
    }

    operator fun invoke(): String = requestPath()

    private fun splitPath(path: String): Pair<List<String>, List<String>> {
        val hasQuery = path.contains("?")
        var segmentPath = if(hasQuery) path.substring(0, path.indexOf("?")) else path
        if(segmentPath.startsWith("/")) {
            segmentPath = segmentPath.substring(1)
        }
        if(segmentPath.endsWith("/")) {
            segmentPath = segmentPath.substring(0, segmentPath.lastIndex)
        }
        val segments = if(segmentPath == "") listOf() else segmentPath.split("/")
        val query = if(!hasQuery) listOf() else path.substring(path.indexOf('?') + 1).split("&")
        return Pair(segments, query)
    }

    override fun toString() = requestPath()

    fun forEach(function: (PathSegment) -> Unit) = segments.forEach(function)

    fun isRoot() = segments.count() == 0
}

