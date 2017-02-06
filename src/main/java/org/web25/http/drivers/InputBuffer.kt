package org.web25.http.drivers

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by felix on 5/11/16.
 */
class InputBuffer @Throws(IOException::class)
constructor(/*private byte[] buffer;
    private int offset;
    private int bytes;*/
        private val inputStream: InputStream) {

    val nextByte: Byte?
        @Throws(IOException::class)
        get() = inputStream.read().toByte()

    @Throws(IOException::class)
    operator fun get(count: Int): ByteArray {
        val data = ByteArray(count)
        var tRead = 0
        while (tRead < count) {
            val pData = ByteArray(if ((count - tRead) % 256 == 0) 256 else (count - tRead) % 256)
            val read = inputStream.read(pData)
            System.arraycopy(pData, 0, data, tRead, read)
            tRead += read
        }
        return data
    }

    @Throws(IOException::class)
    @JvmOverloads fun readUntil(terminator: Byte, skipAfter: Int = 0): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        var data: Int
        data = inputStream.read()
        while (data != terminator.toInt()) {
            byteArrayOutputStream.write(data)
            data = inputStream.read()
        }
        for (i in 0..skipAfter - 1) {
            inputStream.read()
        }
        return byteArrayOutputStream.toString()
    }

    @Throws(IOException::class)
    fun pipe(length: Int, vararg outputStreams: OutputStream) {
        val data = get(length)
        for (outputStream in outputStreams) {
            outputStream.write(data)
        }
    }

}
