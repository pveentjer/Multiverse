package org.multiverse.api.collections;

import org.multiverse.api.Txn;

/**
 * A transactional version of the {@link Iterable}.
 * <p/>
 * For every method the Iterable has, an additional method with a transaction is added. Also the
 * return type is made stronger from Iterator to TxnIterator.
 * <p/>
 * If a method is called, without an explicit transaction being passed, it is retrieved
 * from the {@link org.multiverse.api.TxnThreadLocal}.
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TxnIterable<E> extends Iterable<E> {

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @param txn the Txn used for this Operation.
     * @return an Iterator.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *          if the STM needs to break out from the current
     *          execution, e.g. to retry a transaction because there was a read conflict. This error should not be
     *          caught because it will be caught by the {@link org.multiverse.api.TxnExecutor}.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *          if this operation failed to be executed due
     *          to a programming error.
     */
    TxnIterator<E> iterator(Txn txn);

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *          if the STM needs to break out from the current
     *          execution, e.g. to retry a transaction because there was a read conflict. This error should not be
     *          caught because it will be caught by the {@link org.multiverse.api.TxnExecutor}.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *          if this operation failed to be executed due
     *          to a programming error.
     */
    @Override
    TxnIterator<E> iterator();
}
