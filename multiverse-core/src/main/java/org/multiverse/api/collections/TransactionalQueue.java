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

    E remove(Transaction tx);

    E element(Transaction tx);

    boolean offer(Transaction tx, E item);

    void put(E item);

    void put(Transaction tx, E item);

    E take();

    E take(Transaction tx);

    E poll(Transaction tx);

    E peek(Transaction tx);
}
