package org.web25.http.drivers.treehandler

object MethodMatchers {
    val any : MethodMatcher = {
        true
    }

    val get: MethodMatcher = {
        it.equals("get", true)
    }

    val post: MethodMatcher = {
        it.equals("post", true)
    }

    val update: MethodMatcher = {
        it.equals("update", true)
    }

    val delete: MethodMatcher = {
        it.equals("delete", true)
    }

    val patch: MethodMatcher = {
        it.equals("patch", true)
    }

    val put: MethodMatcher = {
        it.equals("put", true)
    }

    operator fun get(method: String): MethodMatcher {
        return when(method.toLowerCase()) {
            "get" -> get
            "post" -> post
            "update" -> update
            "delete" -> delete
            "patch" -> patch
            "put" -> put
            else -> { methodName: String ->
                methodName.equals(method, true)
            }
        }
    }
}