package io.femo.http.transport.http2;

import io.femo.http.Constants;

import java.io.IOException;

/**
 * Class representing an HTTP/2.0 Frame as defined in RFC 7540, Section 4
 *
 * @author Felix Resch
 * @version 20160903
 */
public class HttpFrame {

    /**
     * The settings for the current HTTP/2.0 connections
     */
    private HttpSettings settings;

    /**
     * The payload length of the frame
     */
    private int length;

    /**
     * The type of the frame
     *
     * Possible types:
     * <table>
     *     <tr>
     *         <thead>Value</thead>
     *         <thead>Frame Type</thead>
     *         <thead>Section</thead>
     *     </tr>
     *     <tr>
     *         <td>0x0</td>
     *         <td>DATA</td>
     *         <td>6.1</td>
     *     </tr>
     *     <tr>
     *         <td>0x1</td>
     *         <td>HEADERS</td>
     *         <td>6.2</td>
     *     </tr>
     *     <tr>
     *         <td>0x2</td>
     *         <td>PRIORITY</td>
     *         <td>6.3</td>
     *     </tr>
     *     <tr>
     *         <td>0x3</td>
     *         <td>RST_STREAM</td>
     *         <td>6.4</td>
     *     </tr>
     *     <tr>
     *         <td>0x4</td>
     *         <td>SETTINGS</td>
     *         <td>6.5</td>
     *     </tr>
     *     <tr>
     *         <td>0x5</td>
     *         <td>PUSH_PROMISE</td>
     *         <td>6.6</td>
     *     </tr>
     *     <tr>
     *         <td>0x6</td>
     *         <td>PING</td>
     *         <td>6.7</td>
     *     </tr>
     *     <tr>
     *         <td>0x7</td>
     *         <td>GOAWAY</td>
     *         <td>6.8</td>
     *     </tr>
     *     <tr>
     *         <td>0x8</td>
     *         <td>WINDOW_UPDATE</td>
     *         <td>6.9</td>
     *     </tr>
     *     <tr>
     *         <td>0x9</td>
     *         <td>CONTINUATION</td>
     *         <td>6.10</td>
     *     </tr>
     * </table>
     */
    private short type;

    /**
     * Flags that are sent with the frame. The interpretation of the flags depends on the type of the frame
     */
    private short flags;

    /**
     * Identifies which stream this frame belongs to
     */
    private int streamIdentifier;

    /**
     * The payload associated with the frame
     */
    private byte[] payload;

    /**
     * Constructs a new HttpFrame with no payload and information
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public HttpFrame(HttpSettings settings) {
        this.settings = settings;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        if(length < 0)
            throw new HttpFrameException("Length of frame (" + length + ") has to be positive");
        if(length > settings.getMaxFrameSize())
            throw new HttpFrameException("Length of frame (" + length + ") cannot exceed maximum frame size of " + settings.getMaxFrameSize() + " defined in session settings");
        if(length > Constants.Http20.MAX_FRAME_LENGTH)
            throw new HttpFrameException("Length of frame (" + length + ") exceeds maximum possible length of " + Constants.Http20.MAX_FRAME_LENGTH);
        this.length = length;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        if(type < 0)
            throw new HttpFrameException("Value of type (" + type + ") has to be positive");
        if(type > 255)
            throw new HttpFrameException("Value of type (" + type + ") exceeds maximum value for a byte (256)");
        //TODO check for available types!
        this.type = type;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        if(flags < 0)
            throw new HttpFrameException("Value of type (" + type + ") has to be positive");
        if(flags > 255)
            throw new HttpFrameException("Value of type (" + type + ") exceeds maximum value for a byte (256)");
        this.flags = flags;
    }

    public int getStreamIdentifier() {
        return streamIdentifier;
    }

    public void setStreamIdentifier(int streamIdentifier) {
        //if(streamIdentifier > Constants.Http20.MAX_STREAM_IDENTIFIER)
        //    throw new HttpFrameException("Value of stream identifier (" + streamIdentifier + ") exceeds maximum value of maximum stream identifier " + Constants.Http20.MAX_STREAM_IDENTIFIER);
        if(streamIdentifier < 0)
            throw new HttpFrameException("Value of stream identifier (" + streamIdentifier + ") has to be positive");
        this.streamIdentifier = streamIdentifier;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        setLength(payload.length);
        this.payload = payload;
    }

    /**
     * This method can be used by subclasses to construct the payload of this frame.
     *
     * It will be called shortly before the payload is sent over a connection and can be called multiple times
     */
    public void build() {

    }
}
