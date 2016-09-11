package io.femo.http.transport.http2;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Created by felix on 9/3/16.
 */
public class HttpUtil {

    @NotNull
    public static byte[] toByte(int data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.putInt(data);
        byteBuffer.flip();
        return byteBuffer.array();
    }

    @NotNull
    public static byte[] toByte(short data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES);
        byteBuffer.putShort(data);
        byteBuffer.flip();
        return byteBuffer.array();
    }

    public static int toInt(byte[] bytes){
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        if(bytes.length < Integer.BYTES) {
            byteBuffer.position(Integer.BYTES - bytes.length);
        }
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer.getInt();
    }

    public static short toShort(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES);
        if(bytes.length < Short.BYTES) {
            byteBuffer.position(Short.BYTES - bytes.length);
        }
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer.getShort();
    }
}
