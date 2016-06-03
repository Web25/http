package io.femo.http.drivers;

import io.femo.http.*;
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

    private static Logger log = LoggerFactory.getLogger("HTTP");

    private String path;

    private IncomingHttpRequest() {
        super();
    }

    @Override
    public String path() {
        return path;
    }


    public static HttpRequest readFromStream(InputStream inputStream) throws IOException {
        InputBuffer inputBuffer = new InputBuffer(inputStream);
        String statusLine = inputBuffer.readUntil((byte) '\r', 1);
        IncomingHttpRequest request = new IncomingHttpRequest();
        parseRequestLine(statusLine, request);
        statusLine = inputBuffer.readUntil((byte) '\r', 1);
        while (!statusLine.equals("")) {
            String name;
            String value;
            name = statusLine.substring(0, statusLine.indexOf(":")).trim();
            value = statusLine.substring(statusLine.indexOf(":") + 1).trim();
            if(name.equals("Cookie")) {
                String cname, cvalue;
                cname = value.substring(0, value.indexOf("="));
                cvalue = value.substring(value.indexOf("=") + 1, !value.contains(";") ? value.length(): value.indexOf(";"));
                request.cookie(cname, cvalue);
            } else {
                request.header(name, value);
            }
            statusLine = inputBuffer.readUntil((byte) '\r', 1);
        }
        if(!request.method().equalsIgnoreCase(Http.GET) && request.hasHeader("Content-Length")) {
            int length = Integer.parseInt(request.header("Content-Length").value());
            if(length == 0) {
                return request;
            }
            request.entity(inputBuffer.get(length));
        }
        return request;
    }

    @NotNull
    private static String readLine(InputStream inputStream) {
        String line = "";
        log.debug("Reading line for HTTP head");
        int read;
        try {
            while ((read = inputStream.read()) != 0) {
                if(read == '\r') {
                    read = inputStream.read();
                    if(read == '\n') {
                        break;
                    } else {
                        continue;
                    }
                }
                if(read == '\n') {
                    log.warn("Received possibly malformed HTTP Request");
                    break;
                }
                line += (char) read;
            }
        } catch (IOException e) {
            log.warn("Exception while reading line from input", e);
        }
        log.debug("Read: " + line);
        return line.trim();
    }

    private static void parseRequestLine(String line, IncomingHttpRequest httpRequest) {
        String method = line.substring(0, line.indexOf(" ")).trim();
        String path = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1)).trim();
        String version = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1).trim();
        httpRequest.method(method);
        httpRequest.path = path;
        if(version.equals("HTTP/1.1")) {
            httpRequest.version(HttpVersion.HTTP_11);
        } else if (version.equals("HTTP/1.0")) {
            httpRequest.version(HttpVersion.HTTP_1);
        }
    }
}
