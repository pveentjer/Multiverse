package org.multiverse.api.collections;

import org.multiverse.api.Transaction;

/**
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TransactionalList<E> extends TransactionalCollection<E>{

    int indexOf(Object item);

    int indexOf(Transaction txn, Object item);

    int lastIndexOf(Object item);

    int lastIndexOf(Transaction txn, Object item);

    E get(int index);

    E get(Transaction txn, int index);

    E set(int index, E element);

    E set(Transaction txn, int index, E element);

    E remove(int index);

    E remove(Transaction txn, int index);
}
