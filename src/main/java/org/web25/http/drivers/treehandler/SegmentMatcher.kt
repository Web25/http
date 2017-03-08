package org.web25.http.drivers.treehandler

import org.web25.http.path.PathSegment

interface SegmentMatcher {
    fun match(pathSegment: String): Boolean
    fun supportsSegment(pathSegment: PathSegment): Boolean
}