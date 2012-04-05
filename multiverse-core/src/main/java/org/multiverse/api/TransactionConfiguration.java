package org.multiverse.api;

import org.multiverse.api.lifecycle.TransactionListener;

import java.util.List;

/**
 * Contains the transaction configuration used by a {@link Transaction}. In the beginning this was all
 * placed in the Transaction, adding a lot of 'informational' methods to the transaction and therefor
 * complicating its usage. So all the configurational properties of the transaction are contained in
 * this structure.
 * <p/>
 * The same TransactionConfiguration is used for multiple transactions. Each TransactionFactory has just a
 * single configuration and all Transactions created by that TransactionFactory, share that configuration.
 *
 * @author Peter Veentjer.
 */
public interface TransactionConfiguration {

    /**
     * Returns the Stm that creates transactions based on this configuration.
     *
     * @return the stm.
     */
    Stm getStm();

    /**
     * Checks if the {@link org.multiverse.api.exceptions.ControlFlowError} is cached or a new one is used.
     * <p/>
     * Exception creation can be very expensive, so by default the ControlFlowError is reused, but this can
     * be problematic when debugging.
     *
     * @return true if the ControlFlowError is reused.
     * @see TransactionFactoryBuilder#setControlFlowErrorsReused(boolean)
     */
    boolean isControlFlowErrorsReused();

    /**
     * Gets the IsolationLevel used. With the IsolationLevel you have control on the isolated behavior between
     * transactions.
     *
     * @return the IsolationLevel.
     * @see TransactionFactoryBuilder#setIsolationLevel(IsolationLevel)
     */
    IsolationLevel getIsolationLevel();

    /**
     * Returns the total timeout in nanoseconds. Long.MAX_VALUE indicates that there is no
     * timeout.
     *
     * @return the total remaining timeout.
     * @see TransactionFactoryBuilder#setTimeoutNs(long)
     */
    long getTimeoutNs();

    /**
     * Returns the PropagationLevel used. With the PropagationLevel you have control on how the transaction
     * is dealing with nesting of transactions.
     *
     * @return the PropagationLevel used.
     * @see org.multiverse.api.TransactionFactoryBuilder#setPropagationLevel(PropagationLevel)
     */
    PropagationLevel getPropagationLevel();

    /**
     * Returns the TraceLevel. With the TraceLevel you have control on the logging.
     *
     * @return the TraceLevel.
     * @see org.multiverse.api.TransactionFactoryBuilder#setTraceLevel(TraceLevel)
     */
    TraceLevel getTraceLevel();

    /**
     * Returns the BackoffPolicy used by the Stm when a transaction conflicts with another transaction.
     *
     * @return the BackoffPolicy used.
     * @see TransactionFactoryBuilder#setBackoffPolicy(BackoffPolicy)
     */
    BackoffPolicy getBackoffPolicy();

    /**
     * Checks if speculative configuration is enabled. When enabled the STM is able to select better
     * performing/scalable implementations at the cost of some
     * {@link org.multiverse.api.exceptions.SpeculativeConfigurationError}. This will be caught by the
     * TransactionExecutor and the transaction will be retried, so in most cases this is not something to worry
     * about, but it can be confusing in the beginning because of unexpected failures in the execution
     * of transactions.
     *
     * @return true if speculative configuration is enabled.
     * @see TransactionFactoryBuilder#setSpeculative(boolean)
     */
    boolean isSpeculative();

    /**
     * Returns the family name of this Transaction. Every transaction in principle should have a family name. This
     * information can be used for debugging/logging purposes but also other techniques that rely to know something
     * about similar types of transactions like profiling.
     *
     * @return the familyName. The returned value can be null.
     * @see TransactionFactoryBuilder#setFamilyName(String)
     */
    String getFamilyName();

    /**
     * Checks if this Transaction is readonly. With a readonly transaction you can prevent any updates or
     * new objects being created.
     *
     * @return true if readonly, false otherwise.
     * @see TransactionFactoryBuilder#setReadonly(boolean)
     */
    boolean isReadonly();

    /**
     * Returns the maximum number of times the transaction is allowed to spin on a read to become
     * readable (perhaps it is locked).
     *
     * @return the maximum number of spins
     * @see org.multiverse.api.TransactionFactoryBuilder#setSpinCount(int)
     */
    int getSpinCount();

    /**
     * Gets the current LockMode for all reads.
     *
     * @return the current LockMode for all reads.
     * @see TransactionFactoryBuilder#setReadLockMode(LockMode)
     */
    LockMode getReadLockMode();

    /**
     * Gets the current LockMode for all writes.
     *
     * @return the current LockMode for all writes.
     * @see TransactionFactoryBuilder#setWriteLockMode(LockMode)
     */
    LockMode getWriteLockMode();

    /**
     * Checks if dirty check is enabled on writes when a transaction commits. Turning of saves time,
     * but forces writes that cause no change.
     *
     * @return true of dirty check is enabled.
     * @see org.multiverse.api.TransactionFactoryBuilder#setDirtyCheckEnabled(boolean)
     */
    boolean isDirtyCheckEnabled();

    /**
     * Checks if this transaction does automatic read tracking. Read tracking is needed for blocking transactions,
     * but also for writeskew detection. Disadvantage of read tracking is that it is more expensive because
     * the reads not to be registered on some datastructure so that they are tracked.
     *
     * @return true if the transaction does automatic read tracking, false otherwise.
     * @see TransactionFactoryBuilder#setReadTrackingEnabled(boolean)
     */
    boolean isReadTrackingEnabled();

    /**
     * If an explicit retry (so a blocking transaction) is allowed. With this property one can prevent
     * that a Transaction is able to block waiting for some change.
     *
     * @return true if explicit retry is allowed, false otherwise.
     * @see TransactionFactoryBuilder#setBlockingAllowed(boolean)
     */
    boolean isBlockingAllowed();

    /**
     * Checks if the Transaction can be interrupted if it is blocking.
     *
     * @return true if the Transaction can be interrupted if it is blocking, false otherwise.
     * @see TransactionFactoryBuilder#setInterruptible(boolean)
     */
    boolean isInterruptible();

    /**
     * Returns an unmodifiable list containing all permanent TransactionListener.
     *
     * @return unmodifiable List containing all permanent TransactionListeners.
     * @see TransactionFactoryBuilder#addPermanentListener(org.multiverse.api.lifecycle.TransactionListener)
     */
    List<TransactionListener> getPermanentListeners();

    /**
     * Returns the maximum number of times this Transaction be retried before failing. The returned value will
     * always be equal or larger than 0. If the value is getAndSet high and you are encountering a lot of
     * TooManyRetryExceptions it could be that the objects are just not concurrent enough.
     *
     * @return the maxRetries.
     * @see TransactionFactoryBuilder#setMaxRetries(int)
     */
    int getMaxRetries();
}
