package io.femo.http;

import io.femo.http.handlers.Authentication;
import io.femo.http.handlers.Handlers;
import io.femo.http.handlers.auth.CredentialProvider;

import java.io.File;

/**
 * Created by felix on 6/13/16.
 */
public class TestApp {

    public static void main(String[] args) {
        File temp = new File("temp");
        if(!temp.exists()) {
            temp.mkdirs();
        }
        /*try {
            Http.get("http://code.jquery.com/jquery-3.1.0.min.js").pipe(new FileOutputStream(new File(temp, "jquery.js"))).execute();
        } catch (FileNotFoundException e) {
            System.err.println("Could not load jquery... Some parts of the website might not be working correctly...");
        }*/
        final HttpServer httpServer = Http.server(8080, true)
                .secure()
                .keyPass("test1234")
                .keystorePass("test1234")
                .keystore("test.keystore")
                //.use(Authentication.digest("test", (uname) -> uname.equals("felix") ? new CredentialProvider.Credentials("felix", "test") : null))
                .get("/", (request, response) -> {
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
                            .header("Content-Type", "text/html");
                    return true;
                })
                .get("/webpage", (request, response) -> {
                    response.entity("<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<title>Hello World</title>" +
                            "<link type=\"text/css\" rel=\"stylesheet\" href=\"/style.css\" />" +
                            "</head>" +
                            "<body>" +
                            "<h1>Hello World</h1>" +
                            "<p>This site's stylesheet has been served using HTTP/2.0 PUSH REQUEST</p>" +
                            "<p>It is ${{iso_datetime}}</p>" +
                            "<p><a href=\"/\">Back</a></p>" +
                            "</body>" +
                            "</html>")
                            .header("Content-Type", "text/html")
                            .push("GET", "/style.css");
                    return true;
                })
                .get("/form", (request, response) -> {
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
                            .header("Content-Type", "text/html");
                    return true;
                })
                .post("/form", (request, response) -> {
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
                            .header("Content-Type", "text/html");
                    return true;
                })
                .get("/restpage", (request, response) -> {
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
                            .push("GET", "/rest/time");
                    return true;
                })
                .get("/cookiepage", (request, response) -> {
                    String entity = "<html>" +
                            "<head>" +
                            "<title>Cookie Test Site</title>" +
                            "</head>" +
                            "<body>";
                    if(request.hasCookie("test1") && request.hasCookie("test2")) {
                        entity += "<p>You have visited this site before</p>";
                    } else {
                        entity += "<p>Visit this site again, to see if cookies are working</p>";
                    }
                    entity += "<p><a href=\"/\">Back</a></p>" +
                            "</body>";
                    response.entity(entity)
                            .cookie("test1", "test1")
                            .cookie("test2", "test2")
                            .header("Content-Type", "text/html");
                    return true;
                })
                .get("/style.css", (request, response) -> {
                    response.entity("body { background-color: black; color: white } a { color: white }")
                            .header("Content-Type", "text/css");
                    return true;
                })
                .get("/jquery.js", Handlers.buffered(new File(temp, "jquery.js"), false, "text/javascript"))
                .use("/secure", Http.router()
                        .use(Authentication.digest("test", (uname) -> uname.equals("felix") ? new CredentialProvider.Credentials("felix", "test") : null))
                        .get("/", (request, response) -> {
                            response.entity("<body>" +
                                    "<p>This is a secure site!</p>" +
                                    "<p><a href=\"/\">Back</a></p>" +
                                    "</body>")
                                    .header("Content-Type", "text/html");
                            return true;
                        })
                )
                .use("/rest", Http.router()
                        .use((request, response) -> {
                            response.header("X-Replace-Env", "true");
                        })
                        .get("/time", (request, response) -> {
                            response.entity("${{iso_datetime}}")
                                    .header("X-Replace-Env", "true");
                            return true;
                        })
                )
                .after(Handlers.environment())
                .after(Handlers.log())
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                httpServer.stop();
            }
        });
    }
}
