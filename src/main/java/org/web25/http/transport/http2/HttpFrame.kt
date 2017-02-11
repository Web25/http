package org.web25.http.transport.http2

import org.web25.http.Constants
import java.util.*

/**
 * Class representing an HTTP/2.0 Frame as defined in RFC 7540, Section 4

 * @author Felix Resch
 * *
 * @version 20160903
 */
open class HttpFrame
/**
 * Constructs a new HttpFrame with no payload and information
 * @param settings the settings for the current HTTP/2,0 connection
 */
(
        /**
         * The settings for the current HTTP/2.0 connections
         */
        val settings: HttpSettings) {

    /**
     * The payload length of the frame
     */
    var length: Int = 0
        set(length) {
            if (length < 0)
                throw HttpFrameException("Length of frame ($length) has to be positive")
            if (length > settings.maxFrameSize)
                throw HttpFrameException("Length of frame (" + length + ") cannot exceed maximum frame size of " + settings.maxFrameSize + " defined in session settings", Constants.Http20.ErrorCodes.FRAME_SIZE_ERROR)
            if (length > Constants.Http20.MAX_FRAME_LENGTH)
                throw HttpFrameException("Length of frame (" + length + ") exceeds maximum possible length of " + Constants.Http20.MAX_FRAME_LENGTH)
            field = length
        }

    /**
     * The type of the frame

     * Possible types:
     * <table>
     * <tr>
     * <thead>Value</thead>
     * <thead>Frame Type</thead>
     * <thead>Section</thead>
    </tr> *
     * <tr>
     * <td>0x0</td>
     * <td>DATA</td>
     * <td>6.1</td>
    </tr> *
     * <tr>
     * <td>0x1</td>
     * <td>HEADERS</td>
     * <td>6.2</td>
    </tr> *
     * <tr>
     * <td>0x2</td>
     * <td>PRIORITY</td>
     * <td>6.3</td>
    </tr> *
     * <tr>
     * <td>0x3</td>
     * <td>RST_STREAM</td>
     * <td>6.4</td>
    </tr> *
     * <tr>
     * <td>0x4</td>
     * <td>SETTINGS</td>
     * <td>6.5</td>
    </tr> *
     * <tr>
     * <td>0x5</td>
     * <td>PUSH_PROMISE</td>
     * <td>6.6</td>
    </tr> *
     * <tr>
     * <td>0x6</td>
     * <td>PING</td>
     * <td>6.7</td>
    </tr> *
     * <tr>
     * <td>0x7</td>
     * <td>GOAWAY</td>
     * <td>6.8</td>
    </tr> *
     * <tr>
     * <td>0x8</td>
     * <td>WINDOW_UPDATE</td>
     * <td>6.9</td>
    </tr> *
     * <tr>
     * <td>0x9</td>
     * <td>CONTINUATION</td>
     * <td>6.10</td>
    </tr> *
    </table> *
     */
    //TODO check for available types!
    var type: Short = 0
        set(type) {
            if (type < 0)
                throw HttpFrameException("Value of type ($type) has to be positive")
            if (type > 255)
                throw HttpFrameException("Value of type ($type) exceeds maximum value for a byte (256)")
            field = type
        }

    /**
     * Flags that are sent with the frame. The interpretation of the flags depends on the type of the frame
     */
    var flags: Short = 0
        set(flags) {
            if (flags < 0)
                throw HttpFrameException("Value of type (" + this.type + ") has to be positive")
            if (flags > 255)
                throw HttpFrameException("Value of type (" + this.type + ") exceeds maximum value for a byte (256)")
            field = flags
        }

    /**
     * Identifies which stream this frame belongs to
     */
    //if(streamIdentifier > Constants.Http20.MAX_STREAM_IDENTIFIER)
    //    throw new HttpFrameException("Value of stream identifier (" + streamIdentifier + ") exceeds maximum value of maximum stream identifier " + Constants.Http20.MAX_STREAM_IDENTIFIER);
    var streamIdentifier: Int = 0
        set(streamIdentifier) {
            if (streamIdentifier < 0)
                throw HttpFrameException("Value of stream identifier ($streamIdentifier) has to be positive")
            field = streamIdentifier
        }

    /**
     * The payload associated with the frame
     */
    var payload: ByteArray = byteArrayOf()
        set(payload) {
            length = payload.size
            field = payload
        }

    /**
     * This method can be used by subclasses to construct the payload of this frame.

     * It will be called shortly before the payload is sent over a connection and can be called multiple times
     */
    open fun build() {

    }

    fun hasEndStreamFlag(): Boolean {
        return type == Constants.Http20.FrameType.HEADERS || type == Constants.Http20.FrameType.DATA
    }

    override fun toString(): String {
        return "HttpFrame{" +
                "length=" + this.length +
                ", type=" + Constants.Http20.findFrameTypeName(this.type) +
                ", flags=" + this.flags +
                ", streamIdentifier=" + this.streamIdentifier +
                ", payload=" + Arrays.toString(this.payload) +
                '}'
    }
}
