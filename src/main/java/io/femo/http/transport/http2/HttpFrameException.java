package io.femo.http.transport.http2;

import io.femo.http.Constants;
import org.jetbrains.annotations.NonNls;

/**
 * Created by felix on 9/3/16.
 */
public class HttpFrameException extends RuntimeException {

    private int errorCode = Constants.Http20.ErrorCodes.PROTOCOL_ERROR;

    public HttpFrameException(@NonNls String message) {
        super(message);
    }

    public HttpFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpFrameException(Throwable cause) {
        super(cause);
    }

    public HttpFrameException(@NonNls String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public HttpFrameException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public HttpFrameException(Throwable cause, int errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
