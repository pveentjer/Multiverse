package org.multiverse.api;

/**
 * The Lock provides access to pessimistic behavior of a {@link TransactionalObject}. STM normally is very optimistic, but
 * in some cases a more pessimistic approach (one with less retries) could be a better fitting solution.
 * <p/>
 * There are 4 different types of lockmodes:
 * <ul>
 * <li><b>LockMode.None:</b> it doesn't do any locking</li>
 * <li><b>LockMode.Read:</b> it allows multiple transactions to acquire the read lock, but transaction acquiring the write-lock
 * or exclusive lock (needed when a transaction wants to commit) is prohibited. If the read lock is acquired by a different
 * transaction, a transaction still is able to read/write, but it isn't allowed to commit the changes (since and exclusive
 * lock is required for that).
 * </li>
 * <li><b>LockMode.Write:</b> it allows only one transaction to acquire the write lock, but unlike a traditional
 * write-lock, reads still are allowed. Normally this would not be acceptable because once the write-lock
 * is acquired, the internals could be modified. But in case of STM, the STM can still provide a consistent view
 * even though the locking transaction has made changes. This essentially is the same behavior you get with the
 * 'select for update' from Oracle. Once the write lock is acquired, other transactions can't acquire the Lock.
 * </li>
 * <li><b>LockMode.Exclusive:</b> it allows only one transaction to acquire the commit lock, and readers are not
 * allowed to read anymore. From an isolation perspective, the exclusive lock looks a lot like the synchronized
 * statement (or a {@link java.util.concurrent.locks.ReentrantLock}} where only mutually exclusive access is
 * possible. The exclusive lock normally is used by the STM when it commits.</li>
 * </ul>
 *
 * <h3>Lock duration and release</h3>
 *
 * <p>Locks atm are acquired for the remaining duration of the transaction and only will always be automatically
 * released once the transaction commits/aborts. This is essentially the same behavior you get with Oracle once
 * a update/delete/insert is done, or when the record is locked manually by executing the 'select for update'. For
 * this to work it is very important that the {@link org.multiverse.api.exceptions.ControlFlowError} is not caught
 * by the logic executed in an atomicChecked block, but is caught by the TransactionExecutor itself.
 *
 * <h3>Blocking</h3>
 *
 * <p>Atm it isn't possible to block on a lock. What happens is that some spinning is done
 * {@link TransactionFactoryBuilder#setSpinCount(int)} and then some retries
 * {@link TransactionFactoryBuilder#setMaxRetries(int)} in combination with a backoff
 * {@link TransactionFactoryBuilder#setBackoffPolicy(BackoffPolicy)}. In the 0.8 release blocking will
 * probably be added.
 *
 * <h3>Fairness</h3>
 *
 * <p>Atm there is no support for fairness. The big problem with fairness and STM is that the locks are released
 * and the transaction needs to begin again. It could be that a lower priority transaction is faster and acquires
 * the lock again. This is a topic that needs more research and probably will be integrated in the contention
 * management.
 *
 * <h3>Lock upgrade</h3>
 *
 * <p>It is possible to upgrade a lock to more strict version, e.g. to upgrade a read-lock to a write-lock.
 * The following upgrades are possible:
 * <ol>
 * <li>LockMode.Read->LockMode.Write: as long as no other transaction has acquired the Lock in LockMode.Read</li>
 * <li>LockMode.Read->LockMode.Exclusive: as long as no other transaction has acquired the Lock in LockMode.Read</li>
 * <li>LockMode.Write->LockMode.Exclusive: will always succeed</li>
 * </ol>
 * <p>
 * The Transaction is allowed to apply a more strict LockMode than the one specified.
 *
 * <h3>Lock downgrade</h3>
 *
 * <p>Downgrading locks currently is not possible and downgrade calls are ignored.
 *
 * <h3>Locking scope</h3>
 *
 * <p>Locking can be done on the Transaction level (see the {@link TransactionFactoryBuilder#setReadLockMode(LockMode)} and
 * {@link TransactionFactoryBuilder#setWriteLockMode(LockMode)} where all reads or all writes (to do a write also a read
 * is needed) are locked automatically. It can also be done on the reference level using
 * getAndLock/setAndLock/getAndSetAndLock methods or by accessing the {@link TransactionalObject#getLock()}.
 *
 * <h3>Lock escalation</h3>
 *
 * <p>In traditional lock based databases, managing locks in memory can be quite expensive. That is one of the reason why
 * different Lock granularities are used (record level, page level, table level for example). To prevent managing too many
 * locks, some databases apply lock escalation so that multiple low granularity locks are upgraded to a single higher granular
 * lock. The problem with lock escalations is that the system could be subject to lock contention and to deadlocks.
 *
 * <p>The GammaStm (the main STM implementation) doesn't use lock escalation, but keeps on managing locks on the transactional object
 * (ref) level.
 *
 * <h3>Deadlocks</h3>
 *
 * <p>2 Ingredients are needed for a deadlock:
 * <ol>
 * <li>Transactions acquiring locks in a different order</li>
 * <li>Transactions that do an unbound waiting for a lock to come available</li>
 * </ol>
 * The problem with applying locks in the same order is that it places an extra borden on the developer. That is why atm the second
 * ingredient is always missing if the GammaStm (the default STM implementation) is used. Therefor a developer doesn't need to worry about
 * deadlocks (although it shifts the problem to an increased chance of starvation and livelocks).
 *
 * @author Peter Veentjer.
 * @see TransactionFactoryBuilder#setReadLockMode(LockMode)
 * @see TransactionFactoryBuilder#setWriteLockMode(LockMode)
 */
public interface Lock {

    /**
     * Returns the current LockMode. This call doesn't look at any running transaction, it shows the actual
     * state of the Lock. The value could be stale as soon as it is received. To retrieve the LockMode a
     * a Transaction has on a Lock, the {@link #getLockMode()} or {@link #getLockMode(Transaction)} need
     * to be used.
     *
     * @return the current LockMode.
     */
    LockMode atomicGetLockMode();

    /**
     * Gets the LockMode the transaction stored in the the {@link ThreadLocalTransaction} has on this Lock.
     * To retrieve the actual LockMode of the Lock, you need to use the {@link #atomicGetLockMode()}.
     *
     * @return the LockMode.
     * @throws org.multiverse.api.exceptions.TransactionExecutionException
     *          if something failed while using the transaction. The transaction is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *          if the Stm needs to control the flow in a different way than normal returns of exceptions. The transaction
     *          is guaranteed to have been aborted.
     * @see #atomicGetLockMode()
     * @see #getLockMode(Transaction)
     */
    LockMode getLockMode();

    /**
     * Gets the LockMode the transaction has on the Lock. This call makes use of the tx. To retrieve the actual
     * LockMode of the Lock, you need to use the {@link #atomicGetLockMode()}
     *
     * @param txn the Lock
     * @return the LockMode the transaction has on the Lock.
     * @throws org.multiverse.api.exceptions.TransactionExecutionException
     *          if something failed while using the transaction. The transaction is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *          if the Stm needs to control the flow in a different way than normal returns of exceptions. The transaction
     *          is guaranteed to have been aborted.
     * @see #atomicGetLockMode()
     * @see #getLockMode(Transaction)
     */
    LockMode getLockMode(Transaction txn);

    /**
     * Acquires a Lock with the provided LockMode. This call doesn't block if the Lock can't be upgraded, but throws
     * a {@link org.multiverse.api.exceptions.ReadWriteConflict}. It could also  be that the Lock is acquired, but the
     * Transaction sees that it isn't consistent anymore. In that case also a
     * {@link org.multiverse.api.exceptions.ReadWriteConflict} is thrown.
     *
     * <p>This call makes use of the Transaction stored in the {@link ThreadLocalTransaction}.
     *
     * <p>If the lockMode is lower than the LockMode the transaction already has on this Lock, the call is ignored.
     *
     * @param desiredLockMode the desired lockMode.
     * @throws org.multiverse.api.exceptions.TransactionExecutionException
     *                              if something failed while using the transaction. The transaction is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                              if the Stm needs to control the flow in a different way than normal returns of exceptions. The transaction
     *                              is guaranteed to have been aborted.
     * @throws NullPointerException if desiredLockMode is null. If an alive transaction is available, it will
     *                              be aborted.
     */
    void acquire(LockMode desiredLockMode);

    /**
     * Acquires a Lock with the provided LockMode using the provided transaction. This call doesn't block if the Lock can't be
     * upgraded but throws a {@link org.multiverse.api.exceptions.ReadWriteConflict}. It could also be that the Lock is acquired,
     * but the Transaction sees that it isn't consistent anymore. In that case also a
     * {@link org.multiverse.api.exceptions.ReadWriteConflict} is thrown.
     *
     * <p>If the lockMode is lower than the LockMode the transaction already has on this Lock, the call is ignored.
     *
     * @param txn              the Transaction used for this operation.
     * @param desiredLockMode the desired lockMode.
     * @throws org.multiverse.api.exceptions.TransactionExecutionException
     *                              if something failed while using the transaction. The transaction is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                              if the Stm needs to control the flow in a different way than normal returns of exceptions. The transaction
     *                              is guaranteed to have been aborted.
     * @throws NullPointerException if tx or desiredLockMode is null. If an alive transaction is available, it will
     *                              be aborted.
     */
    void acquire(Transaction txn, LockMode desiredLockMode);
}
