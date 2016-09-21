package io.femo.http.transport.http2.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by felix on 9/21/16.
 */
public class DebugInputStream extends InputStream {

    private InputStream base;

    private AtomicReference<PrintStream> debugFile;
    private AtomicIntegerArray data = new AtomicIntegerArray(16);
    private AtomicInteger index = new AtomicInteger(0);

    public DebugInputStream(InputStream base) {
        this.base = base;
        try {
            this.debugFile = new AtomicReference<>(new PrintStream("http20_in.dump"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int read() throws IOException {
        int data = base.read();
        //dump(data);
        return data;
    }

    private void dump(int data) {
        if(debugFile != null) {
            this.data.set(index.get(), data);
            if (index.incrementAndGet() == 15) {
                writeLine();
            }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        writeLine();
    }

    private void writeLine() {
        if(debugFile != null) {
            PrintStream debugFile = this.debugFile.get();
            synchronized (debugFile) {
                for (int i = 0; i <= index.get(); i++) {
                    debugFile.printf("%02x ", this.data.get(i));
                }
                for (int i = index.get(); i <= 15; i++) {
                    debugFile.print("   ");
                }
                debugFile.print("| ");
                for (int i = 0; i <= index.get(); i++) {
                    if(this.data.get(i) < 32) {
                        debugFile.print(".");
                    } else {
                        debugFile.print((char) this.data.get(i));
                    }
                }
                debugFile.println();
            }
            index.set(0);
        }
    }


}
