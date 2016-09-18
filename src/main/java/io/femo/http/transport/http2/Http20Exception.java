package io.femo.http.transport.http2;

import io.femo.http.Constants;
import org.jetbrains.annotations.NonNls;

/**
 * Created by felix on 9/16/16.
 */
public class Http20Exception extends RuntimeException {

    private int errorCode = Constants.Http20.ErrorCodes.PROTOCOL_ERROR;

    public Http20Exception(@NonNls String message) {
        super(message);
    }

    public Http20Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Http20Exception(Throwable cause) {
        super(cause);
    }

    public Http20Exception(@NonNls String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public Http20Exception(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public Http20Exception(Throwable cause, int errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
