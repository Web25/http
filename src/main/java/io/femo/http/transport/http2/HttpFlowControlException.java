package io.femo.http.transport.http2;

import org.jetbrains.annotations.NonNls;

/**
 * Created by felix on 9/4/16.
 */
public class HttpFlowControlException extends RuntimeException {

    public HttpFlowControlException(@NonNls String message) {
        super(message);
    }

    public HttpFlowControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpFlowControlException(Throwable cause) {
        super(cause);
    }
}
