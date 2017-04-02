package org.web25.http.drivers.treehandler

import org.web25.http.path.PathSegment
import org.web25.http.path.StaticSegment

class StaticSegmentMatcher(val segment: String): SegmentMatcher {

    override fun match(pathSegment: String): Boolean = segment == pathSegment

    override fun supportsSegment(pathSegment: PathSegment): Boolean = pathSegment is StaticSegment && pathSegment.segment == segment

    override fun toString(): String = "static:$segment"
}