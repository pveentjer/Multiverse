package org.multiverse.api.collections;

import org.multiverse.api.Transaction;

import java.util.Queue;

/**
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TransactionalQueue<E> extends TransactionalCollection<E>, Queue<E> {

    int getCapacity();

    E remove(Transaction txn);

    E element(Transaction txn);

    boolean offer(Transaction tx, E item);

    void put(E item);

    void put(Transaction txn, E item);

    E take();

    E take(Transaction txn);

    E poll(Transaction txn);

    E peek(Transaction txn);
}
