package io.femo.http;

import io.femo.http.handlers.Authentication;
import io.femo.http.handlers.Handlers;
import io.femo.http.handlers.auth.CredentialProvider;

/**
 * Created by felix on 6/13/16.
 */
public class TestApp {

    public static void main(String[] args) {
        Http.server(8080, true)
                .secure()
                .keyPass("test1234")
                .keystorePass("test1234")
                .keystore("test.keystore")
                //.use(Authentication.digest("test", (uname) -> uname.equals("felix") ? new CredentialProvider.Credentials("felix", "test") : null))
                .get("/", (request, response) -> {
                    response.entity("<html>" +
                            "<head>" +
                            "<title>Web 2.5 HTTP Server/title>" +
                            "</head>" +
                            "<body>" +
                            "<h1>Web 2.5 HTTP Server</h1>" +
                            "<strong>Test pages</strong>" +
                            "<ul>" +
                            "<li><a href=\"/webpage\">Page with PUSH_REQUEST</a></li>" +
                            "<li><a href=\"/secure/\">Page with HTTP Authentication</a></li>" +
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
                .get("/style.css", (request, response) -> {
                    response.entity("body { background-color: black; color: white }")
                            .header("Content-Type", "text/css");
                    return true;
                }).use("/secure", Http.router()
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
                .after(Handlers.environment())
                .after(Handlers.log())
                .start();
    }
}
