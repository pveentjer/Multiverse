package org.multiverse.api.collections;

import org.multiverse.api.Transaction;

/**
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TransactionalList<E> extends TransactionalCollection<E>{

    int indexOf(Object item);

    int indexOf(Transaction tx, Object item);

    int lastIndexOf(Object item);

    int lastIndexOf(Transaction tx, Object item);

    E get(int index);

    E get(Transaction tx, int index);

    E set(int index, E element);

    E set(Transaction tx, int index, E element);

    E remove(int index);

    E remove(Transaction tx, int index);
}
