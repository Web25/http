package io.femo.http.drivers.server;

import java.util.concurrent.Future;

/**
 * Created by felix on 9/19/16.
 */
public interface ExecutorListener {

    void submit(Runnable runnable);
}
