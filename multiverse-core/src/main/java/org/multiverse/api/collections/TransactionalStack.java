package org.multiverse.api.collections;

import org.multiverse.api.Transaction;

/**
 *
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TransactionalStack<E> extends TransactionalCollection<E> {

    int getCapacity();

    void push(E item);

    void push(Transaction tx, E item);

    boolean offer(E item);

    boolean offer(Transaction tx, E item);

    E pop();

    E pop(Transaction tx);

    E poll();

    E poll(Transaction tx);

    E peek();

    E peek(Transaction tx);
}
