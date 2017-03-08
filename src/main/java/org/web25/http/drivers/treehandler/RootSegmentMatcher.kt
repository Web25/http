package org.web25.http.drivers.treehandler

import org.web25.http.path.PathSegment

class RootSegmentMatcher: SegmentMatcher {
    override fun match(pathSegment: String): Boolean = pathSegment == ""

    override fun supportsSegment(pathSegment: PathSegment): Boolean = false

    override fun toString(): String = "root"
}