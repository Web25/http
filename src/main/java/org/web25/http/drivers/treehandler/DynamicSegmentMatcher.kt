package org.web25.http.drivers.treehandler

import org.web25.http.path.DynamicSegment
import org.web25.http.path.PathSegment
import org.web25.http.path.StaticSegment

class DynamicSegmentMatcher(val segment: DynamicSegment): SegmentMatcher {

    override fun match(pathSegment: String): Boolean = true

    override fun supportsSegment(pathSegment: PathSegment): Boolean {
        pathSegment.path[segment.key] = (pathSegment as StaticSegment).segment
        return true
    }

    override fun toString(): String = "dyn:${segment.key}"
}