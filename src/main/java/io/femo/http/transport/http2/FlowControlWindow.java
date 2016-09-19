package io.femo.http.transport.http2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by felix on 9/4/16.
 */
public class FlowControlWindow {

    private FlowControlWindow parent;

    private AtomicInteger local;
    private AtomicInteger remote;
    private HttpConnection connection;

    public FlowControlWindow(HttpConnection connection) {
        this.connection = connection;
        this.local = new AtomicInteger(connection.getLocalSettings().getInitialWindowSize());
        this.remote = new AtomicInteger(connection.getRemoteSettings().getInitialWindowSize());
    }

    public FlowControlWindow(FlowControlWindow parent, HttpConnection connection) {
        this(connection);
        this.parent = parent;
    }

    public boolean checkIncoming(int length) {
        if(parent != null) {
            if(!parent.checkIncoming(length)) {
                return false;
            }
        }
        return local.get() >= length;
    }

    public boolean checkOutgoing(int length) {
        if(parent != null) {
            if(!parent.checkOutgoing(length)) {
                return false;
            }
        }
        return remote.get() >= length;
    }

    public void decreaseLocal(int length) {
        if(!checkIncoming(length))
            throw new HttpFlowControlException("The local flow control window is too small to support the incoming DATA frame!");
        if(parent != null) {
            parent.decreaseLocal(length);
        }
        local.updateAndGet(i -> i - length);
    }

    public void decreaseRemote(int length) {
        if(!checkOutgoing(length))
            throw new HttpFlowControlException("The remote flow control window is too small to support the outgoing DATA frame");
        if(parent != null) {
            parent.decreaseRemote(length);
        }
        remote.updateAndGet(i -> i - length);
    }

    public void incrementLocal(int length) {
        local.updateAndGet(i -> i + length);
    }

    public void incremetRemote(int length) {
        remote.updateAndGet(i -> i + length);
    }
}
