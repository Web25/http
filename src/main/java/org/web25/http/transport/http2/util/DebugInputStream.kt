package org.web25.http.transport.http2.util

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by felix on 9/21/16.
 */
class DebugInputStream(private val base: InputStream) : InputStream() {

    private var debugFile: AtomicReference<PrintStream>? = null
    private val data = AtomicIntegerArray(16)
    private val index = AtomicInteger(0)

    init {
        try {
            this.debugFile = AtomicReference(PrintStream("http20_in.dump"))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }


    @Throws(IOException::class)
    override fun read(): Int {
        val data = base.read()
        //dump(data);
        return data
    }

    private fun dump(data: Int) {
        if (debugFile != null) {
            this.data.set(index.get(), data)
            if (index.incrementAndGet() == 15) {
                writeLine()
            }
        }
    }

    @Throws(IOException::class)
    override fun close() {
        super.close()
        writeLine()
    }

    private fun writeLine() {
        if (debugFile != null) {
            val debugFile = this.debugFile!!.get()
            synchronized(debugFile) {
                for (i in 0..index.get()) {
                    debugFile.printf("%02x ", this.data.get(i))
                }
                for (i in index.get()..15) {
                    debugFile.print("   ")
                }
                debugFile.print("| ")
                for (i in 0..index.get()) {
                    if (this.data.get(i) < 32) {
                        debugFile.print(".")
                    } else {
                        debugFile.print(this.data.get(i).toChar())
                    }
                }
                debugFile.println()
            }
            index.set(0)
        }
    }


}
