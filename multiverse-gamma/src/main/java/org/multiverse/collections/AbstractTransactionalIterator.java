package org.multiverse.collections;

import org.multiverse.api.collections.TransactionalIterator;

import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public abstract class AbstractTransactionalIterator<E> implements TransactionalIterator<E> {

    @Override
    public boolean hasNext() {
        return hasNext(getThreadLocalTransaction());
    }

    @Override
    public E next() {
        return next(getThreadLocalTransaction());
    }

    @Override
    public void remove() {
        remove(getThreadLocalTransaction());
    }
}
