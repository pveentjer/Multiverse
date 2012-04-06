package org.multiverse.api;

/**
 * With the IsolationLevel you have a way to provide declarative control to guarantee isolation between transactions.
 * The transaction is free to provide a higher isolation than the one specified.
 * <p/>
 * The dirty read isn't added since atm we already have an extremely cheap read using the atomicWeakGet on the
 * refs. Using the atomicWeakGet you have extremely cheap access to committed data.
 *
 * <p>The following isolation anomalies have been identified:
 * <ol>
 *     <li>Dirty Read</li>
 *     <li>Unrepeatable Read</li>
 *     <li>Inconsistent Read</li>
 *     <li>Write Skew</li>
 * </ol>
 *
 * <h3>Dirty Read</h3>
 *
 * <p>The DirtyRead isolation anomaly is that one transaction could observe uncommitted changes made by another transaction.
 * The biggest problem with this anomaly is that eventually the updating transaction could abort and the reading transaction
 * sees changes that never made it into the system.
 *
 * <p>Currently the Dirty Read anomaly is not possible in Multiverse since writes to main memory are deferred until commit
 * and once the first write to main memory is executed, the following writes are guaranteed to succeed.
 *
 * <h3>Unrepeatable Read</h3>
 *
 * <p>The Unrepeatable Read isolation anomaly is that a transaction could see changes made by other transactions. So at one
 * moment it could see value X and a moment later it could see Y. This could lead to all kinds of erratic behavior like
 * transaction that can get stuck in an infinitive loop.
 *
 * <p>Such a transaction is called a zombie transaction and can cause serious damage since they are consuming resources
 * (like cpu) and are holding on to various resources (like Locks). So the unrepeatable read should be used with care.
 *
 * <h3>Inconsistent Read</h3>
 *
 * <p>With the inconsistent read it is possible that a value is read depending on a previous read value that has been updated
 * by another transaction. E.g. there are 100 refs all initialized with the same value and there is an updating transaction
 * that increments all value's by one, and there is a reading transaction that reads all refs, then it should see the same
 * values for all values.
 *
 * <h3>Writeskew</h3>
 *
 * <p>With the writeskew problem it is possible that 2 transactions commit....
 *
 * <p>Example 1: there are 1 person that has 2 bank accounts and the invariant is that the sum of both bank accounts is
 * always equal or larger than zero. If user has has 50 euro on both accounts (so the sum is 100) and there are 2 transactions
 * that both subtract 100 euro from the bank accounts, and the first sees account1=50 and account2=50, the subtraction is allowed
 * and subtracts it 100 from the first account, and the second transaction sees exactly the same and subtracts 100 from the second
 * account, it could be possible that the person ends up with -50 on both accounts (so a sum of -100), clearly violating the
 * contract that the the sum should never be smaller than 0.
 *
 * <p>Example 2:
 *
 * <h3>Isolation Levels</h3>
 *
 * <p>The following isolation levels are currently defined in Multiverse:
 * <ol>
 *     <li>Read Committed</li>
 *     <li>Repeatable Read</li>
 *     <li>Snapshot</li>
 *     <li>Serialized</li>
 * </ol>
 * The read uncommitted is currently not defined since currently there is no need to do a dirty read, there will always be committed
 * data available for reading (the write is deferred till commit, so you won't have to see transactions in progress).
 *
 * <table>
 *
 * </table>
 *
 *
 * <h3>Implementation and isolation level upgrade</h3>
 *
 * <p>An implementation of the {@link Transaction} is free to upgrade the isolation level to a higher one if it doesn't support that specific isolation
 * level. This is the same as Oracle is doing with the ReadUncommitted, which automatically is upgraded to a ReadCommitted or the RepeatableRead which is
 * automatically upgraded to Snapshot (Oracle calls this the Serialized isolation level).
 *
 * <h3>Isolation: pessimistic or optimistic</h3>
 *
 * <h3>Isolation through locking</h3>
 *
 *
 *
 *
 * @author Peter Veentjer.
 * @see TxnFactoryBuilder#setIsolationLevel(IsolationLevel)
 */
public enum IsolationLevel {

    /**
     * With the RepeatableRead isolation level you will always see committed data and next to that once a read
     * is done, this read is going to be repeatable (so you will see the same value every time).
     */
    RepeatableRead(false, true, true),

    /**
     * The default isolation level that allows for the writeskew problem but not for dirty or unrepeatable
     * or inconsistent reads.
     * <p/>
     * This is the 'serialized' isolation level provided by MVCC databases like Oracle/Postgresql
     * (although Postgresql 9 is going to provide a truly serialized isolation level) and MySQL with the InnoDb.
     * All data read. contains committed data and all data will be consistent.
     * <p/>
     * A transaction that is readonly, gets the same isolation behavior as the Serializable isolation level
     * since the writeskew problem can't occur (nothing can be written).
     */
    Snapshot(false, false, true),

    /**
     * Provides truly serialized transaction at the cost of reduced performance and concurrency. This is the highest
     * isolation level where no isolation anomalies are allowed to happen. So the writeSkew problem is not allowed to
     * happen.
     */
    Serializable(false, false, false);

    private final boolean allowWriteSkew;
    private final boolean allowUnrepeatableRead;
    private final boolean allowInconsistentRead;

    IsolationLevel(boolean allowUnrepeatableRead, boolean allowInconsistentRead, boolean allowWriteSkew) {
        this.allowUnrepeatableRead = allowUnrepeatableRead;
        this.allowInconsistentRead = allowInconsistentRead;
        this.allowWriteSkew = allowWriteSkew;
    }

    /**
     * Checks if the writeskew problem is allowed to happen.
     *
     * @return true if the writeSkew is allowed to happen.
     */
    public final boolean doesAllowWriteSkew() {
        return allowWriteSkew;
    }

    /**
     * Checks if the dirty read is allowed to happen (so reading data that has not been committed).
     *
     * @return true if the dirty read is allowed to happen.
     */
    public boolean doesAllowUnrepeatableRead() {
        return allowUnrepeatableRead;
    }

    /**
     * Checks if the inconsistent read is allowed to happen.
     *
     * @return true if the inconsistent read is allowed to happen.
     */
    public boolean doesAllowInconsistentRead() {
        return allowInconsistentRead;
    }

    @Override
    public String toString() {
        return "IsolationLevel." + name();
    }
}
