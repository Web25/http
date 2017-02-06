package org.web25.http.transport.http2

import org.slf4j.LoggerFactory
import org.web25.http.Constants
import org.web25.http.transport.http2.frames.SettingsFrame

/**
 * Created by felix on 9/3/16.
 */
class HttpSettings {

    var headerTableSize = 4096
    var isEnablePush = true
    var maxConcurrentStreams = Integer.MAX_VALUE
    var initialWindowSize = 65535
    var maxFrameSize = 16384
    var maxHeaderListSize = Integer.MAX_VALUE


    var endpointType: EndpointType? = null
        private set
    var isInitiator: Boolean = false
        private set

    constructor(endpointType: EndpointType) {
        this.endpointType = endpointType
        this.isInitiator = false
    }

    constructor(initiator: Boolean) {
        this.endpointType = EndpointType.PEER
        this.isInitiator = initiator
    }

    fun apply(frame: SettingsFrame) {
        frame.forEach { setting ->
            log.debug("Updating " + Constants.findSettingName(setting.identifier) + " to " + setting.value)
            when (setting.identifier) {
                Constants.Http20.SettingIdentifiers.SETTINGS_HEADER_TABLE_SIZE -> {
                    this.headerTableSize = setting.value
                }
                Constants.Http20.SettingIdentifiers.SETTINGS_ENABLE_PUSH -> {
                    if (setting.value == 0) {
                        this.isEnablePush = false
                    } else if (setting.value == 1) {
                        this.isEnablePush = true
                    } else {
                        throw Http20Exception("Invalid value for setting enable push")
                    }
                }
                Constants.Http20.SettingIdentifiers.SETTINGS_MAX_CONCURRENT_STREAMS -> {
                    this.maxConcurrentStreams = setting.value
                }
                Constants.Http20.SettingIdentifiers.SETTINGS_INITIAL_WINDOW_SIZE -> {
                    if (setting.value < 0) {
                        throw Http20Exception("Invalid value for setting initial window size", Constants.Http20.ErrorCodes.FLOW_CONTROL_ERROR)
                    }
                    this.initialWindowSize = setting.value
                }
                Constants.Http20.SettingIdentifiers.SETTINGS_MAX_FRAME_SIZE -> {
                    if (setting.value > 16777215 || setting.value < 16384) {
                        throw Http20Exception("Invalid value for setting max frame size")
                    }
                    this.maxFrameSize = setting.value
                }
                Constants.Http20.SettingIdentifiers.SETTINGS_MAX_HEADER_LIST_SIZE -> {
                    this.maxHeaderListSize = setting.value
                }
                else -> {

                }
            }
        }
    }

    enum class EndpointType {

        SERVER,

        CLIENT,
        /**
         * This state defines that the connection is used as a WTP connection, allowing both devices to perform requests,
         * responses and push requests.
         */
        PEER
    }

    override fun toString(): String {
        return "HttpSettings{" +
                "headerTableSize=" + headerTableSize +
                ", enablePush=" + isEnablePush +
                ", maxConcurrentStreams=" + maxConcurrentStreams +
                ", initialWindowSize=" + initialWindowSize +
                ", maxFrameSize=" + maxFrameSize +
                ", maxHeaderListSize=" + maxHeaderListSize +
                ", endpointType=" + endpointType +
                ", initiator=" + isInitiator +
                '}'
    }

    companion object {

        val log = LoggerFactory.getLogger("HTTP/2.0")
    }
}
