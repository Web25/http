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

        public class FrameType {

            public static final short DATA = 0x0;
            public static final short SETTINGS = 0x4;
        }
    }
}
