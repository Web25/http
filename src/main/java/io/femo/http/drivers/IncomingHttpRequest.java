package io.femo.http.drivers;

import io.femo.http.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by felix on 2/25/16.
 */
public class IncomingHttpRequest extends DefaultHttpRequest {

    private String path;

    public IncomingHttpRequest() {
        super();
    }

    @Override
    public String path() {
        return path;
    }

    public IncomingHttpRequest path(String path) {
        this.path = path;
        return this;
    }

    public IncomingHttpRequest appendBytes(byte[] data) {
        String contentLength = hasHeader("Content-Length") ? header("Content-Length").value() : null;
        if(entityBytes() == null) {
            entity(data);
        } else {
            byte[] payload = entityBytes();
            byte[] dst = new byte[payload.length + data.length];
            System.arraycopy(dst, 0, payload, 0, payload.length);
            System.arraycopy(dst, payload.length, data, 0, data.length);
            entity(dst);
        }
        if(contentLength != null)
            header("Content-Length", contentLength);
        return this;
    }
}
