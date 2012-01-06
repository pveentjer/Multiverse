package org.multiverse.api.collections;

import org.multiverse.api.Transaction;

/**
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TransactionalIterable<E> extends Iterable<E> {

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @param tx the Transaction used for this Operation.
     * @return an Iterator.
     */
    TransactionalIterator<E> iterator(Transaction tx);

    @Override
    TransactionalIterator<E> iterator();
}
