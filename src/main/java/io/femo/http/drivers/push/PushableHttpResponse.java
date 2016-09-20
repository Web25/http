package io.femo.http.drivers.push;

import io.femo.http.HttpRequest;
import io.femo.http.HttpResponse;
import io.femo.http.drivers.DefaultHttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 9/19/16.
 */
public class PushableHttpResponse extends DefaultHttpResponse {

    private List<PushRequest> pushRequests;

    public PushableHttpResponse(HttpRequest httpRequest) {
        this.request(httpRequest);
        this.pushRequests = new ArrayList<>();
    }

    @Override
    public HttpResponse push(String method, String path) {
        PushRequest pushRequest = new PushRequest(request());
        pushRequest.method(method);
        pushRequest.path(path);
        pushRequests.add(pushRequest);
        return this;
    }

    public List<PushRequest> getPushRequests() {
        return pushRequests;
    }
}
