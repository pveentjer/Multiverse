package org.multiverse.api.collections;

import org.multiverse.api.Txn;

/**
 *
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TxnStack<E> extends TxnCollection<E> {

    int getCapacity();

    void push(E item);

    void push(Txn txn, E item);

    boolean offer(E item);

    boolean offer(Txn txn, E item);

    E pop();

    E pop(Txn txn);

    E poll();

    E poll(Txn txn);

    E peek();

    E peek(Txn txn);
}
