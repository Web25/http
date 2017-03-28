package org.web25.http

/**
 * Created by felix on 3/28/17.
 */

fun main(args: Array<String>) {
    val http = Http()
    val req = http.get("https://keybase.io/_/api/1.0/user/lookup.json")
    req.query["usernames"] = "reschfelix"
    val res = req()
    print(res.responseString())
}