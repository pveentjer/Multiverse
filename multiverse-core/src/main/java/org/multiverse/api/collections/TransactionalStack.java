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

    void push(Transaction txn, E item);

    boolean offer(E item);

    boolean offer(Transaction txn, E item);

    E pop();

    E pop(Transaction txn);

    E poll();

    E poll(Transaction txn);

    E peek();

    E peek(Transaction txn);
}
