# Web 2.5 HTTP Library

[![Build Status](https://travis-ci.org/Web25/http.svg?branch=master)](https://travis-ci.org/Web25/http)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.femo/http/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.femo/http)

This library provides a simple API to perform various HTTP operations. As well as serving as a client you can also start a simple server that performs simple handling of HTTP requests in versions 1.1 and 2.0.

Support for **HTTP/2.0** is only available for the server but will soon be available for the client as well.

As of version *0.1.0* the project has switched vendor and is now written in Kotlin. It is still compatible with all JVM based languages but requires `kotlin-stdlib` and `kotlin-reflection` to function properly.

## GET Requests

Calls in kotlin are as simple as in Java. To perform a simple `GET` request on `http://example.org` use this snippet.

```kotlin
val http = Http()
val response = http.get("http://example.org/").response()
```

And for Java use this

```java
Http http = new Http()
HttpResponse response = http.get("http://example.org/").response();
```
        
The request will be executed and the result cached in the HttpResponse object. 
 
    if(response.statusCode() == StatusCode.OK) {
        //Response was successfull
    } else {
        //Response was not successfull
    }
        
To retrieve the content of the response use
 
        System.out.println(response.responseString());
        
## POST Request

To perform a simple HTTP POST use the following call.

```kotlin
val http = Http()
http.post("http://example.org/post").response()
```
        
To append data use
```kotlin
http
    .post("http://example.org/post")
    .data("test", "test")
    .response()
```
        
            
The data is automatically UrlFormEncoded and sent to the server.

## Drivers

### Use with Android

        Http.installDriver(new AndroidDriver());
        
### Asynchronous Use
This driver spawns one new Thread to execute each request. Use this only for projects with few requests as it generates heavy load.

        Http.installDriver(new AsynchronousDriver()); 
        
### Asynchronous Batch Use
This driver creates a Thread Executor Service to execute requests in the Background. Use this driver for projects that 
perform a huge amount of requests.

        Http.installDriver(new AsynchronousDriver(5));
        
You have to supply the constructor with the amount of executor threads you want to spawn at the start of the program.

## HTTP Server

To start a simple HTTP Server on any port simply call the *server(port:int)* of the Http class and start the server
  
        Http.server(8080).start();
        
This will return an object of type HttpServer and will start an HTTP server on port 8080 that 404s every request.

To provide some content, you can use any of the *use* methods offered by HttpServer. For simplicity standard HTTP methods have predefined methods to use.

        server.get('/', (request, response) -> {response.entity("Hello World); return true}) //This print Hello World to any clients requesting GET /
        
        server.post('/', (req, res) -> {res.entity(req.requestBytes()); return true;}); //This echos everything a client sends to POST /
        
To provide funtionality for a whole path, or for all requests middleware can be used. To log every request code like this could be used:

        server.use((HttpMiddleware) (req, res) -> System.out.println(req.method() + " " + req.path()));
        
If you also want to log information that is available after a request has been handled, use the HttpServer.after() method.

        server.after((request, response) -> {
                  System.out.printf("%03d %-4s %s - %s\n", response.statusCode(), request.method(), request.path(),
                          response.hasHeader("Content-Length") ? response.header("Content-Length").value() : " -- ");
              })

To stop a running instance use the *stop()* method.

        server.stop()
        
The server will stop all it's listeners and threads after all pending requests have been handled.