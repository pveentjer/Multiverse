package org.multiverse.collections;

import org.multiverse.api.collections.TxnIterator;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public abstract class AbstractTxnIterator<E> implements TxnIterator<E> {

    @Override
    public boolean hasNext() {
        return hasNext(getThreadLocalTxn());
    }

    @Override
    public E next() {
        return next(getThreadLocalTxn());
    }

    @Override
    public void remove() {
        remove(getThreadLocalTxn());
    }
}
