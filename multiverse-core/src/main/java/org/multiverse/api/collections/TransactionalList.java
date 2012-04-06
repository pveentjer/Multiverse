package org.multiverse.api.collections;

import org.multiverse.api.Txn;

/**
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TransactionalList<E> extends TransactionalCollection<E>{

    int indexOf(Object item);

    int indexOf(Txn txn, Object item);

    int lastIndexOf(Object item);

    int lastIndexOf(Txn txn, Object item);

    E get(int index);

    E get(Txn txn, int index);

    E set(int index, E element);

    E set(Txn txn, int index, E element);

    E remove(int index);

    E remove(Txn txn, int index);
}
