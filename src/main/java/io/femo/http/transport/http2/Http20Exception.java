package io.femo.http.transport.http2;

import org.jetbrains.annotations.NonNls;

/**
 * Created by felix on 9/16/16.
 */
public class Http20Exception extends RuntimeException {

    public Http20Exception(@NonNls String message) {
        super(message);
    }

    public Http20Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Http20Exception(Throwable cause) {
        super(cause);
    }
}
