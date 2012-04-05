package org.multiverse.api;

/**
 * The interface each transactional object needs to implement.
 *
 * <p>A TransactionalObject is an object where all reads/writes are managed through a {@link Transaction} (unless an atomicChecked
 * method is used).
 *
 * <p>Each TransactionalObject belongs to 1 {@link Stm} instance.
 *
 * <p>All methods are threadsafe.
 *
 * @author Peter Veentjer.
 */
public interface TransactionalObject {

    /**
     * Returns the {@link Stm} this TransactionalObject is part of. Once a TransactionalObject is created using some
     * Stm, it will never become part of another Stm.
     *
     * @return the Stm this TransactionalObject is part of. Returned value will never be null.
     */
    Stm getStm();

    /**
     * Gets the {@link Lock} that belongs to this TransactionalObject. This call doesn't cause any locking, it
     * only provides access to the object that is able to lock. The returned value will never be null.
     *
     * <p>This call also doesn't rely on a {@link Transaction}.
     *
     * @return the Lock
     * @throws UnsupportedOperationException if this operation is not supported.
     */
    Lock getLock();

    /**
     * Returns the current version of the transactional object. Each time an update happens, the value is increased. It depends
     * on the stm implementation if the version over references has any meaning. With the MVCC there is a relation, but with the
     * SkySTM isn't.
     *
     * <p>This method doesn't look at the {@link ThreadLocalTransaction}.
     *
     * @return the current version.
     */
    long getVersion();

    /**
     * Does an ensure. What is means is that at the end of the transaction (so deferred), the transaction checks if no other
     * transaction has made an update, if this TransactionalObject only is read. The ensure is a way to prevent to writeskew
     * problem on the ref level (see {@link IsolationLevel} for more detail about the writeskew problem}
     *
     * <p>This can safely be called on an TransactionalObject that already is locked, although it doesn't provide much value
     * since with a locked TransactionalObject, since the writeskew problem can't occur anymore because it can't be changed.
     *
     * <p>Unlike the {@link Lock#acquire(LockMode)} which is pessimistic, ensure is optimistic. This means that a conflict
     * can be detected once the transaction commits.
     *
     * <p>This method has no effect if the {@link Transaction} is readonly, because a writeskew is not possible with a
     * readonly transaction.
     *
     * <p>This call lifts on the {@link Transaction} stored in the {@link ThreadLocalTransaction}.
     *
     * @throws org.multiverse.api.exceptions.TransactionExecutionException
     *
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *
     * @see #ensure(Transaction)
     */
    void ensure();

    /**
     * Does an ensure. What is means is that at the end of the transaction (so deferred), the transaction checks if no other
     * transaction has made an update, if this TransactionalObject only is read. The ensure is a way to prevent to writeskew
     * problem on the ref level (see {@link IsolationLevel} for more detail about the writeskew problem}
     *
     * <p>This can safely be called on an TransactionalObject that already is locked, although it doesn't provide much value
     * since with a locked TransactionalObject, since the writeskew problem can't occur anymore because it can't be changed.
     *
     * <p>Unlike the {@link Lock#acquire(LockMode)} which is pessimistic, ensure is optimistic. This means that a conflict
     * can be detected once the transaction commits.
     *
     * <p>This method has no effect if the {@link Transaction} is readonly, because a writeskew is not possible with a
     * readonly transaction.
     *
     * @param self the Transaction this call lifts on.
     * @throws NullPointerException if self is null.
     * @throws org.multiverse.api.exceptions.TransactionExecutionException
     *
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *
     * @see #ensure()
     */
    void ensure(Transaction self);

    /**
     * Returns a debug representation of the TransactionalObject. The data used doesn't have to be consistent,
     * it is a best effort. This method doesn't rely on a running transaction.
     *
     * @return the debug representation of the TransactionalObject.
     */
    String toDebugString();

    /**
     * Returns a String representation of the Object using the {@link Transaction} on the {@link ThreadLocalTransaction}.
     *
     * @return the toString representation
     * @throws org.multiverse.api.exceptions.TransactionExecutionException
     *
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *
     */
    @Override
    String toString();

    /**
     * Returns a String representation of the object using the provided {@link Transaction}.
     *
     * @param tx the Transaction used.
     * @return the String representation of the object.
     * @throws NullPointerException if tx is null.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *
     */
    String toString(Transaction tx);

    /**
     * Returns a String representation of the object using the provided transaction without looking
     * at a {@link ThreadLocalTransaction}. The outputted value doesn't need to be consistent from some point
     * in time, only a best effort is made.
     *
     * @return the String representation.
     */
    String atomicToString();
}
