package org.web25.http.drivers.treehandler

import org.slf4j.LoggerFactory
import org.web25.http.StatusCode
import org.web25.http.exceptions.HttpHandleException
import org.web25.http.path.DynamicSegment
import org.web25.http.path.HttpPath
import org.web25.http.path.PathSegment
import org.web25.http.path.StaticSegment
import org.web25.http.server.*
import org.web25.http.util.Stack

/**
 * Created by felix on 3/7/17.
 */
class TreeHandler: IncomingRequestHandler {

    val root = TreeHandlerNode(null)
    private val fallbacks = mutableListOf<HttpHandler>()

    private val log = LoggerFactory.getLogger("HTTP")

    fun submit(path: HttpPath, requestHandler: RequestHandler) {
        if(path.isRoot()) {
            root.handlers.add(Pair(RootSegmentMatcher(), requestHandler))
        } else {
            var current = root
            val segments = path.segments.iterator()
            while (segments.hasNext()) {
                val segment = segments.next()
                if(segments.hasNext()) {
                    current = current.findOrCreateChild(segment)
                } else {
                    if(path.endsWithSlash) {
                        current = current.findOrCreateChild(segment)
                        current.handlers.add(Pair(RootSegmentMatcher(), requestHandler))
                    } else {
                        if(segment is StaticSegment) {
                            val matcher = StaticSegmentMatcher(segment.segment)
                            current.handlers.add(Pair(matcher, requestHandler))
                        } else if (segment is DynamicSegment) {
                            current.handlers.add(Pair(DynamicSegmentMatcher(segment), requestHandler))
                        }
                    }
                }
            }
        }
    }

    fun injectChild(path: HttpPath, treeHandlerNode: TreeHandlerNode) {
        var current = root
        val segments = path.segments.iterator()
        while (segments.hasNext()) {
            val segment = segments.next()
            if(segments.hasNext()) {
                current = current.findOrCreateChild(segment)
            } else {
                val matcher: SegmentMatcher
                if(segment is StaticSegment) {
                    matcher = StaticSegmentMatcher(segment.segment)
                } else if (segment is DynamicSegment) {
                    matcher = DynamicSegmentMatcher(segment)
                } else {
                    TODO()
                }
                current.children.add(Pair(matcher, treeHandlerNode))
            }
        }
    }

    private fun rootHandler(request: IncomingHttpRequest, response: OutgoingHttpResponse, currentStack: Stack<TreeHandlerNode>): Boolean {
        val node = currentStack.peek()!!
        node.handlers.filter {
            it.first is RootSegmentMatcher
        }
        .map { it.second }
        .filter { it.matches(request) }
        .forEach {
            try {
                if (it.handle(request, response)) {
                    currentStack.popEach {
                        it.afters.forEach {
                            it.handle(request, response)
                        }
                    }
                    return true
                }
            } catch (e: HttpHandleException) {
                log.warn("Error in handling", e)
                response.status(e.statusCode?: StatusCode.INTERNAL_SERVER_ERROR)
                response.entity(e.message?: "Unknown error")
                return true
            }
        }
        return false
    }

    private fun segmentHandler(request: IncomingHttpRequest, response: OutgoingHttpResponse, currentStack: Stack<TreeHandlerNode>, segment: PathSegment): Boolean {
        val node = currentStack.peek()!!
        node.handlers.filter {
            it.first.supportsSegment(segment)
        }
        .map { it.second }
        .filter { it.matches(request) }
        .forEach {
            try {
                if (it.handle(request, response)) {
                    currentStack.popEach {
                        it.afters.forEach {
                            it.handle(request, response)
                        }
                    }
                    return true
                }
            } catch (e: HttpHandleException) {
                log.warn("Error in handling", e)
                response.status(e.statusCode?: StatusCode.INTERNAL_SERVER_ERROR)
                response.entity(e.message?: "Unknown error")
                return true
            }
        }
        return false
    }

    override fun handle(request: IncomingHttpRequest, response: OutgoingHttpResponse): Boolean {
        val path = request.path
        val segments = path.segments.iterator()
        val currentStack = Stack(mutableListOf(root))
        if(path.isRoot()) {
            if(rootHandler(request, response, currentStack))
                return true
        }
        while (segments.hasNext()) {
            val current = currentStack.peek()!!
            val segment = segments.next()
            if(segments.hasNext()) {
                current.handlers.filter {
                    it.first.supportsSegment(segment) && it.second is MiddlewareRequestHandler
                }.forEach {
                    it.second.handle(request, response)
                }
                val children = current.children.filter {
                    it.first.supportsSegment(segment)
                }
                if(children.isEmpty()) {
                    return false
                } else if (children.size == 1) {
                    currentStack.push(children.first().second)
                } else {
                    throw RuntimeException("Multiple bindings for children found!")
                }
            } else {
                if(path.endsWithSlash) {
                    val children = current.children.filter {
                        it.first.supportsSegment(segment)
                    }
                    if(children.isEmpty()) {
                        return false
                    } else if (children.size == 1) {
                        currentStack.push(children.first().second)
                    } else {
                        throw RuntimeException("Multiple bindings for children found!")
                    }
                    if(rootHandler(request, response, currentStack))
                        return true
                } else {
                    if(segmentHandler(request, response, currentStack, segment))
                        return true
                }
            }
        }
        return fallbacks.any { it.invoke(request, response) }
    }

    fun fallback(handler: HttpHandler) {
        fallbacks.add(handler)
    }

    fun submitAfter(middleware: HttpMiddleware) {
        root.afters.add(MiddlewareRequestHandler(middleware))
    }

}


