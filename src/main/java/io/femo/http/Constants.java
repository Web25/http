package io.femo.http;

/**
 * Created by felix on 7/2/16.
 */
public final class Constants {

    public static final String VERSION = "0.0.3";

    public final class Http20 {

        /**
         * Max frame length is limited by the 24 bit width of the length field in a frame
         *
         * RFC 7540, Section 4.1
         */
        public static final int MAX_FRAME_LENGTH = 16777216;

        /**
         * Max stream identifier, limited by the 31 bit width of the stream identifier field in a frame
         *
         * RFC 7540, Section 4.1
         */
        public static final long MAX_STREAM_IDENTIFIER = 0x80000000;

        /**
         * The length of the header of an HTTP/2.0 frame
         *
         * RFC 7540, Section 4.1
         */
        public static final int FRAME_HEADER_LENGTH = 9;

        public final class FrameType {

            public static final short DATA = 0x0;
            public static final short PRIORITY = 0x2;
            public static final short RST_STREAM = 0x3;
            public static final short SETTINGS = 0x4;
            public static final short WINDOW_UPDATE = 0x9;
            public static final short GOAWAY = 0x7;

        }

        public final class ErrorCodes {

            public static final int NO_ERROR = 0x0;
            public static final int PROTOCOL_ERROR = 0x1;
            public static final int INTERNAL_ERROR = 0x2;
            public static final int FLOW_CONTROL_ERROR = 0x3;
            public static final int SETTINGS_TIMEOUT = 0x4;
            public static final int STREAM_CLOSED = 0x5;
            public static final int FRAME_SIZE_ERROR = 0x6;
            public static final int REFUSED_STREAM = 0x7;
            public static final int CANCEL = 0x8;
            public static final int COMPRESION_ERROR = 0x9;
            public static final int CONNECT_ERROR = 0xa;
            public static final int ENHANCE_YOUR_CALM = 0xb;
            public static final int INADEQUATE_SECURITY = 0xc;
            public static final int HTTP_1_1_REQUIRED = 0xd;


        }
    }
}
