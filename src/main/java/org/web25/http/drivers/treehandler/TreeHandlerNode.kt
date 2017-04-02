package org.web25.http.drivers.treehandler

import org.web25.http.path.DynamicSegment
import org.web25.http.path.PathSegment
import org.web25.http.path.StaticSegment

class TreeHandlerNode(var parent: TreeHandlerNode?) {

    val children = mutableListOf<Pair<SegmentMatcher, TreeHandlerNode>>()
    val handlers = mutableListOf<Pair<SegmentMatcher, RequestHandler>>()
    val afters = mutableListOf<RequestHandler>()

    fun findOrCreateChild(pathSegment: PathSegment): TreeHandlerNode {
        val matches = children.filter { it.first.supportsSegment(pathSegment) }
        if(matches.count() >= 1) {
            return matches.first().second
        } else {
            if(pathSegment is StaticSegment) {
                val node = TreeHandlerNode(this)
                val matcher = StaticSegmentMatcher(pathSegment.segment)
                children.add(Pair(matcher, node))
                return node
            } else if (pathSegment is DynamicSegment) {
                val node = TreeHandlerNode(this)
                val matcher = DynamicSegmentMatcher(pathSegment)
                children.add(Pair(matcher, node))
                return node
            } else {
                TODO()
            }
        }
    }

}