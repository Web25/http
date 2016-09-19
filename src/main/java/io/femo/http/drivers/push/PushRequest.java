package io.femo.http.drivers.push;

import io.femo.http.HttpRequest;
import io.femo.http.drivers.IncomingHttpRequest;

/**
 * Created by felix on 9/19/16.
 */
public class PushRequest extends IncomingHttpRequest {

    public PushRequest (HttpRequest httpRequest) {
        if(httpRequest.hasHeader("User-Agent"))
            header("User-Agent", httpRequest.header("User-Agent").value());
        if(httpRequest.hasHeader(":authority"))
            header(":authority", httpRequest.header(":authority").value());
        if(httpRequest.hasHeader(":scheme"))
            header(":scheme", httpRequest.header(":scheme").value());
    }
}
