package io.femo.http;

public class AliasTest {
    public static void main(String[] args){
        HttpServer httpServer = Http.server(8080).get("/", (request, response) -> {
            response.entity("Without value\n");
            return true;
        }).get("/{value}", (request, response) -> {
            String answer = "With value: "+request.path().substring(1)+"\n";
            response.entity(answer);
            return true;
        }).start();
    }
}
