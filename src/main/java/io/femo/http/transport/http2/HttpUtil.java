package io.femo.http.transport.http2;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by felix on 9/3/16.
 */
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger("HTTP/2.0");

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

    public static boolean containsUppercase(String text) {
        for(char c : text.toCharArray()) {
            if(Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }
}
