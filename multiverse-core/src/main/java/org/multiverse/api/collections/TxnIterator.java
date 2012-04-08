package org.multiverse.api.collections;

import org.multiverse.api.Txn;

import java.util.Iterator;

/**
 * A Transactional version of the {@link Iterator}. For every method in the
 * Iterator interface, a new method with a transaction parameter has been added.
 * <p/>
 * If a method is called, without an explicit transaction being passed, it is retrieved
 * from the {@link org.multiverse.api.TxnThreadLocal}.
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TxnIterator<E> extends Iterator<E> {

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @param txn transaction used for this operation
     * @return <tt>true</tt> if the iterator has more elements.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                              if the STM needs to break out from the current
     *                              execution, e.g. to retry a transaction because there was a read conflict. This error should not be
     *                              caught because it will be caught by the {@link org.multiverse.api.TxnExecutor}.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                              if this operation failed to be executed due
     *                              to a programming error.
     */
    boolean hasNext(Txn txn);

    /**
     * Returns the next element in the iteration.
     *
     * @param txn transaction used for this operation
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *                              iteration has no more elements.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                              if the STM needs to break out from the current
     *                              execution, e.g. to retry a transaction because there was a read conflict. This error should not be
     *                              caught because it will be caught by the {@link org.multiverse.api.TxnExecutor}.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                              if this operation failed to be executed due
     *                              to a programming error.
     */
    E next(Txn txn);

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @param txn transaction used for this operation
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *                                       operation is not supported by this Iterator.
     * @throws IllegalStateException         if the <tt>next</tt> method has not
     *                                       yet been called, or the <tt>remove</tt> method has already
     *                                       been called after the last call to the <tt>next</tt>
     *                                       method.
     * @throws NullPointerException          if txn is null.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                                       if the STM needs to break out from the current
     *                                       execution, e.g. to retry a transaction because there was a read conflict. This error should not be
     *                                       caught because it will be caught by the {@link org.multiverse.api.TxnExecutor}.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                                       if this operation failed to be executed due
     *                                       to a programming error.
     */
    void remove(Txn txn);
}
