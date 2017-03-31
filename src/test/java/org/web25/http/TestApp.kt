package org.web25.http

import org.web25.http.handlers.Handlers
import org.web25.http.handlers.auth.CredentialProvider
import org.web25.http.util.handler
import org.web25.http.util.middleware
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Created by felix on 6/13/16.
 */
object TestApp {

    @JvmStatic fun main(args: Array<String>) {
        val temp = File("temp")
        if (!temp.exists()) {
            temp.mkdirs()
        }
        /*try {
            Http.get("http://code.jquery.com/jquery-3.1.0.min.js").pipe(new FileOutputStream(new File(temp, "jquery.js"))).execute();
        } catch (FileNotFoundException e) {
            System.err.println("Could not load jquery... Some parts of the website might not be working correctly...");
        }*/
        val http = Http()
        val cookieRouter = http.router()
                .get("/", handler { _, response ->
                    response.cookie(HttpCookie("lang", "en-US", expires = ZonedDateTime.of(2021, 6, 9, 10, 18, 14, 0, ZoneId.of("GMT")), path = "/cookie/", domain = "localhost", secure = true,
                            httpOnly = true))
                    true
                })
        val httpServer = http.server(8080)
                .secure()
                .keyPass("test1234")
                .keystorePass("test1234")
                .keystore("test.keystore")
                //.fallback(Authentication.digest("test", (uname) -> uname.equals("felix") ? new CredentialProvider.Credentials("felix", "test") : null))
                .get("/", handler { _, response ->
                    response.entity("<html>" +
                            "<head>" +
                            "<title>Web 2.5 HTTP Server</title>" +
                            "</head>" +
                            "<body>" +
                            "<h1>Web 2.5 HTTP Server</h1>" +
                            "<strong>Test pages</strong>" +
                            "<ul>" +
                            "<li><a href=\"/webpage\">Page with PUSH_REQUEST</a></li>" +
                            "<li><a href=\"/secure/\">Page with HTTP Authentication</a></li>" +
                            "<li><a href=\"/form\">Page with POST Form</a></li>" +
                            "<li><a href=\"/restpage\">Page with REST Call</a></li>" +
                            "<li><a href=\"/cookiepage\">Page with Cookies</a></li>" +
                            "</ul>" +
                            "</body>")
                            .header("Content-Type", "text/html")
                    true
                })
                .get("/webpage", handler { _, response ->
                    response.entity("<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<title>Hello World</title>" +
                            "<link type=\"text/css\" rel=\"stylesheet\" href=\"/style.css\" />" +
                            "</head>" +
                            "<body>" +
                            "<h1>Hello World</h1>" +
                            "<p>This site's stylesheet has been served using HTTP/2.0 PUSH REQUEST</p>" +
                            "<p>It is \${{iso_datetime}}</p>" +
                            "<p><a href=\"/\">Back</a></p>" +
                            "</body>" +
                            "</html>")
                            .header("Content-Type", "text/html")
                            .push("GET", "/style.css")
                    true
                })
                .get("/form", handler { _, response ->
                    response.entity("<html>" +
                            "<head><title>POST Form Site</title></head>" +
                            "<body>" +
                            "<h1>POST Form Site</h1>" +
                            "<form method=\"POST\">" +
                            "<input type=\"text\" name=\"name\" /><br />" +
                            "<button type=\"submit\">Send</button>" +
                            "</form>" +
                            "<p><a href=\"/\">Back</a></p>" +
                            "</body>" +
                            "</html>")
                            .header("Content-Type", "text/html")
                    true
                })
                .post("/form", handler { request, response ->
                    response.entity("<html>" +
                            "<head><title>POST Form Site</title></head>" +
                            "<body>" +
                            "<h1>POST Form Site</h1>" +
                            "<strong>Entity:</strong>" +
                            "<p>" +
                            request.entityString() +
                            "</p>" +
                            "<p><a href=\"/\">Back</a></p>" +
                            "</body>" +
                            "</html>")
                            .header("Content-Type", "text/html")
                    true
                })
                .get("/restpage", handler { _, response ->
                    response.entity("<html>" +
                            "<head>" +
                            "<title>REST Test Site</title>" +
                            "<script src=\"/jquery.js\"></script>" +
                            "</head>" +
                            "<body>" +
                            "<p>Current time:</p>" +
                            "<p id=\"time\"></p>" +
                            "<p><a href=\"/\">Back</a></p>" +
                            "<script>" +
                            "setInterval(function() {" +
                            "$('#time').load('/rest/time')" +
                            "}, 500);" +
                            "</script>" +
                            "</body>" +
                            "</html>")
                            .header("Content-Type", "text/html")
                            .push("GET", "/jquery.js")
                            .push("GET", "/rest/time")
                    true
                })
                .get("/cookiepage", handler { request, response ->
                    var entity = "<html>" +
                            "<head>" +
                            "<title>Cookie Test Site</title>" +
                            "</head>" +
                            "<body>"
                    if ("test1" in request.cookies && "test2" in request.cookies) {
                        entity += "<p>You have visited this site before</p>"
                    } else {
                        entity += "<p>Visit this site again, to see if cookies are working</p>"
                    }
                    entity += "<p><a href=\"/\">Back</a></p>" + "</body>"
                    response.entity(entity)
                            .cookie("test1", "test1")
                            .cookie("test2", "test2")
                            .header("Content-Type", "text/html")
                    true
                })
                .get("/style.css", handler { _, response ->
                    response.entity("body { background-color: black; color: white } a { color: white }")
                            .header("Content-Type", "text/css")
                    true
                })
                .get("/jquery.js", Handlers.buffered(File(temp, "jquery.js"), false, "text/javascript"))
                .use("/secure", http.router()
                        .use(org.web25.http.handlers.Authentication.digest("test", object : CredentialProvider {
                            override fun findByUsername(username: String): CredentialProvider.Credentials {
                                if(username == "felix") {
                                    return CredentialProvider.Credentials("felix", "test")
                                } else {
                                    throw RuntimeException("No Credentials found!")
                                }
                            }
                        }))
                        .get("/", handler { _, response ->
                            response.entity("<body>" +
                                    "<p>This is a secure site!</p>" +
                                    "<p><a href=\"/\">Back</a></p>" +
                                    "</body>")
                                    .header("Content-Type", "text/html")
                            true
                        })
                )
                .use("/rest", http.router()
                        .use(middleware { _, response -> response.header("X-Replace-Env", "true")})
                        .get("/time", handler { _, response ->
                            response.entity("\${{iso_datetime}}")
                                    .header("X-Replace-Env", "true")
                            true
                        })
                )
                .use("/cookie", cookieRouter)
                .after(Handlers.log())
                .start()
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                httpServer.stop()
            }
        })
    }
}
