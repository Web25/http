package io.femo.http.transport.http2;

import org.jetbrains.annotations.NonNls;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameException extends RuntimeException {

    public HttpFrameException(@NonNls String message) {
        super(message);
    }

    public HttpFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpFrameException(Throwable cause) {
        super(cause);
    }
}
