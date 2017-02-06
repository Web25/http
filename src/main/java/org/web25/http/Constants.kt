package org.web25.http

import org.jetbrains.annotations.Contract

/**
 * Created by felix on 7/2/16.
 */
class Constants {

    class Http20 {

        object FrameType {

            val DATA: Short = 0x0
            val HEADERS: Short = 0x1
            val PRIORITY: Short = 0x2
            val RST_STREAM: Short = 0x3
            val SETTINGS: Short = 0x4
            val PUSH_PROMISE: Short = 0x5
            val PING: Short = 0x6
            val GOAWAY: Short = 0x7
            val WINDOW_UPDATE: Short = 0x8
            val CONTINUATION: Short = 0x9


        }

        object ErrorCodes {

            val NO_ERROR = 0x0
            val PROTOCOL_ERROR = 0x1
            val INTERNAL_ERROR = 0x2
            val FLOW_CONTROL_ERROR = 0x3
            val SETTINGS_TIMEOUT = 0x4
            val STREAM_CLOSED = 0x5
            val FRAME_SIZE_ERROR = 0x6
            val REFUSED_STREAM = 0x7
            val CANCEL = 0x8
            val COMPRESION_ERROR = 0x9
            val CONNECT_ERROR = 0xa
            val ENHANCE_YOUR_CALM = 0xb
            val INADEQUATE_SECURITY = 0xc
            val HTTP_1_1_REQUIRED = 0xd


        }

        object SettingIdentifiers {

            val SETTINGS_HEADER_TABLE_SIZE = 0x1
            val SETTINGS_ENABLE_PUSH = 0x2
            val SETTINGS_MAX_CONCURRENT_STREAMS = 0x3
            val SETTINGS_INITIAL_WINDOW_SIZE = 0x4
            val SETTINGS_MAX_FRAME_SIZE = 0x5
            val SETTINGS_MAX_HEADER_LIST_SIZE = 0x6
        }

        companion object {

            /**
             * Max frame length is limited by the 24 bit width of the length field in a frame

             * RFC 7540, Section 4.1
             */
            val MAX_FRAME_LENGTH = 16777216

            /**
             * Max stream identifier, limited by the 31 bit width of the stream identifier field in a frame

             * RFC 7540, Section 4.1
             */
            val MAX_STREAM_IDENTIFIER: Long = 0x80000000

            /**
             * The length of the header of an HTTP/2.0 frame

             * RFC 7540, Section 4.1
             */
            val FRAME_HEADER_LENGTH = 9
        }
    }

    companion object {

        val VERSION = "0.1.0"

        @Contract(pure = true)
        fun findFrameTypeName(frameType: Short): String {
            when (frameType) {
                Http20.FrameType.SETTINGS -> {
                    return "SETTINGS"
                }
                Http20.FrameType.DATA -> {
                    return "DATA"
                }
                Http20.FrameType.CONTINUATION -> {
                    return "CONTINUATION"
                }
                Http20.FrameType.HEADERS -> {
                    return "HEADERS"
                }
                Http20.FrameType.GOAWAY -> {
                    return "GOAWAY"
                }
                Http20.FrameType.PRIORITY -> {
                    return "PRIORITY"
                }
                Http20.FrameType.RST_STREAM -> {
                    return "RST_STREAM"
                }
                Http20.FrameType.WINDOW_UPDATE -> {
                    return "WINDOW_UPDATE"
                }
                Http20.FrameType.PING -> {
                    return "PING"
                }
                Http20.FrameType.PUSH_PROMISE -> {
                    return "PUSH_PROMISE"
                }
                else -> return "UNKNOWN [$frameType]"
            }
        }

        @Contract(pure = true)
        fun findSettingName(settingIdentifier: Int): String {
            when (settingIdentifier) {
                Http20.SettingIdentifiers.SETTINGS_HEADER_TABLE_SIZE -> {
                    return "SETTINGS_HEADER_TABLE_SIZE"
                }
                Http20.SettingIdentifiers.SETTINGS_ENABLE_PUSH -> {
                    return "SETTINGS_ENABLE_PUSH"
                }
                Http20.SettingIdentifiers.SETTINGS_MAX_CONCURRENT_STREAMS -> {
                    return "SETTINGS_MAX_CONCURRENT_STREAMS"
                }
                Http20.SettingIdentifiers.SETTINGS_INITIAL_WINDOW_SIZE -> {
                    return "SETTINGS_INITIAL_WINDOW_SIZE"
                }
                Http20.SettingIdentifiers.SETTINGS_MAX_FRAME_SIZE -> {
                    return "SETTINGS_MAX_FRAME_SIZE"
                }
                Http20.SettingIdentifiers.SETTINGS_MAX_HEADER_LIST_SIZE -> {
                    return "SETTINGS_MAX_HEADER_LIST_SIZE"
                }
                else -> return "UNKNOWN [$settingIdentifier]"
            }
        }
    }
}
