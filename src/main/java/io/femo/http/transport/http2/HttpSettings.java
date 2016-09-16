package io.femo.http.transport.http2;

import io.femo.http.Constants;
import io.femo.http.transport.http2.frames.SettingsFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by felix on 9/3/16.
 */
public class HttpSettings {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

    private int headerTableSize = 4096;
    private boolean enablePush = true;
    private int maxConcurrentStreams = Integer.MAX_VALUE;
    private int initialWindowSize = 65535;
    private int maxFrameSize = 16384;
    private int maxHeaderListSize = Integer.MAX_VALUE;


    private EndpointType endpointType;
    private boolean initiator;

    public HttpSettings(EndpointType endpointType) {
        this.endpointType = endpointType;
        this.initiator = false;
    }

    public HttpSettings(boolean initiator) {
        this.endpointType = EndpointType.PEER;
        this.initiator = initiator;
    }

    public boolean isInitiator() {
        return initiator;
    }

    public EndpointType getEndpointType() {
        return endpointType;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public void setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    public void apply(SettingsFrame frame) {
        frame.forEach(s -> {
            log.debug("Updating " + Constants.findSettingName(s.getIdentifier()) + " to " + s.getValue());
            switch (s.getIdentifier()) {
                case Constants.Http20.SettingIdentifiers.SETTINGS_HEADER_TABLE_SIZE: {
                    this.headerTableSize = s.getValue();
                    break;
                }
                case Constants.Http20.SettingIdentifiers.SETTINGS_ENABLE_PUSH: {
                    if(s.getValue() == 0) {
                        this.enablePush = false;
                    } else if (s.getValue() == 1) {
                        this.enablePush = true;
                    } else {
                        throw new Http20Exception("Invalid value for setting enable push");
                    }
                    break;
                }
                case Constants.Http20.SettingIdentifiers.SETTINGS_MAX_CONCURRENT_STREAMS: {
                    this.maxConcurrentStreams = s.getValue();
                    break;
                }
                case Constants.Http20.SettingIdentifiers.SETTINGS_INITIAL_WINDOW_SIZE: {
                    if(s.getValue() < 0) {
                        throw new Http20Exception("Invalid value for setting initial window size");
                    }
                    this.initialWindowSize = s.getValue();
                    break;
                }
                case Constants.Http20.SettingIdentifiers.SETTINGS_MAX_FRAME_SIZE: {
                    if(s.getValue() > 16777215 || s.getValue() < 16384) {
                        throw new Http20Exception("Invalid value for setting max frame size");
                    }
                    this.maxFrameSize = s.getValue();
                    break;
                }
                case Constants.Http20.SettingIdentifiers.SETTINGS_MAX_HEADER_LIST_SIZE: {
                    this.maxHeaderListSize = s.getValue();
                    break;
                }
                default: {

                }
            }
        });
    }

    public enum EndpointType {

        SERVER,

        CLIENT,
        /**
         * This state defines that the connection is used as a WTP connection, allowing both devices to perform requests,
         * responses and push requests.
         */
        PEER
    }
}
