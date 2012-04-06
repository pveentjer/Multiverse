package org.multiverse.api.collections;

import org.multiverse.api.Txn;

import java.util.Queue;

/**
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TxnQueue<E> extends TxnCollection<E>, Queue<E> {

    int getCapacity();

    E remove(Txn txn);

    E element(Txn txn);

    boolean offer(Txn tx, E item);

    void put(E item);

    void put(Txn txn, E item);

    E take();

    E take(Txn txn);

    E poll(Txn txn);

    E peek(Txn txn);
}
