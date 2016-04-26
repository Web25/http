package io.femo.http.drivers.server;

import io.femo.http.HttpHandleException;
import io.femo.http.HttpMiddleware;
import io.femo.http.HttpRequest;
import io.femo.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 4/25/16.
 */
public class HttpHandlerStack {

    private Logger log = LoggerFactory.getLogger("HTTP");

    private List<HttpHandle> httpHandlerHandles;

    private List<HttpMiddleware> after;

    public HttpHandlerStack() {
        this.httpHandlerHandles = new ArrayList<>();
        this.after = new ArrayList<>();
    }

    public void submit(HttpHandle httpHandle) {
        this.httpHandlerHandles.add(httpHandle);
    }

    public void submitAfter(HttpMiddleware httpMiddleware) {
        after.add(httpMiddleware);
    }

    public void handle(HttpRequest httpRequest, HttpResponse httpResponse) {
        for (HttpHandle httpHandle : httpHandlerHandles) {
            if (httpHandle.matches(httpRequest)) {
                try {
                    if (httpHandle.handle(httpRequest, httpResponse)) {
                        break;
                    }
                } catch (HttpHandleException e) {
                    log.warn("Error while handling HTTP request", e);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    e.printStackTrace(new PrintStream(byteArrayOutputStream));
                    httpResponse.status(e.getStatusCode());
                    httpResponse.entity(byteArrayOutputStream.toByteArray());
                }
            }
        }
        for(HttpMiddleware middleware : after) {
            try {
                middleware.handle(httpRequest, httpResponse);
            } catch (HttpHandleException e) {
                log.warn("Error while performing finalizing operations on HTTP request", e);
            }
        }
    }
}
