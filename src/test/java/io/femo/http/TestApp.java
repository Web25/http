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
                .use(Authentication.digest("test", (uname) -> uname.equals("felix") ? new CredentialProvider.Credentials("felix", "test") : null))
                .get("/", (request, response) -> {
                    response.entity("Hello World ${{iso_datetime}}");
                    response.header("X-Replace-Env", "true");
                    return true;
                })
                .after(Handlers.environment())
                .after(Handlers.log())
                .start();
    }
}
