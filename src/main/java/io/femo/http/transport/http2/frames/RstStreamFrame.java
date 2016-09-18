package io.femo.http.transport.http2.frames;

import io.femo.http.Constants;
import io.femo.http.transport.http2.HttpFrame;
import io.femo.http.transport.http2.HttpFrameException;
import io.femo.http.transport.http2.HttpSettings;
import io.femo.http.transport.http2.HttpUtil;

/**
 * Created by felix on 9/18/16.
 */
public class RstStreamFrame extends HttpFrame {

    private int errorCode;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public RstStreamFrame(HttpSettings settings) {
        this(settings, Constants.Http20.ErrorCodes.NO_ERROR);
    }

    public RstStreamFrame(HttpSettings settings, int errorCode) {
        super(settings);
        setType(Constants.Http20.FrameType.RST_STREAM);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public void build() {
        setPayload(HttpUtil.toByte(errorCode));
    }

    public static RstStreamFrame from(HttpFrame frame) {
        if(frame.getLength() != 4) {
            throw new HttpFrameException("Invalid length for RST_STREAM frame");
        }
        if(frame.getType() != Constants.Http20.FrameType.RST_STREAM) {
            throw new HttpFrameException("Invalid type for RST_STREAM frame");
        }
        RstStreamFrame rstStreamFrame = new RstStreamFrame(frame.getSettings());
        rstStreamFrame.setStreamIdentifier(frame.getStreamIdentifier());
        rstStreamFrame.setFlags(frame.getFlags());
        rstStreamFrame.setErrorCode(HttpUtil.toInt(frame.getPayload()));
        return rstStreamFrame;
    }
}
