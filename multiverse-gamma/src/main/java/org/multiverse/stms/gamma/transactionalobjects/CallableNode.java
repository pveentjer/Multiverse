package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.functions.Function;

public final class CallableNode {
    public CallableNode next;
    public Function function;

    public CallableNode() {
    }

    public CallableNode(Function function, CallableNode next) {
        this.next = next;
        this.function = function;
    }

    public void prepareForPooling() {
        next = null;
        function = null;
    }
}
