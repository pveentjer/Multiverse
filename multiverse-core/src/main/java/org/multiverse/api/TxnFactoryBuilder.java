package org.multiverse.api;

import org.multiverse.api.lifecycle.TxnListener;

/**
 * A Builder for creating a {@link TxnFactory} and {@link TxnExecutor}. This builder provides full control
 * on transaction settings.
 *
 * <p>Since the {@link Txn} and {@link TxnExecutor} are very closely integrated, both of them are created
 * by this TxnFactoryBuilder.
 *
 * <p>Instances of this class are considered immutable, so when you call one of the modifying methods, make sure
 * that you use the resulting TxnFactoryBuilder. Normally with the builder implementation the same
 * instance is returned. In this case this isn't true because a new instance is returned every time.
 *
 * @author Peter Veentjer
 * @see TxnFactory
 * @see TxnConfig
 */
public interface TxnFactoryBuilder {

    /**
     * Returns the {@link TxnConfig} used by this TxnFactoryBuilder.
     *
     * @return the used TxnConfig.
     */
    TxnConfig getConfiguration();

    /**
     * Sets if the {@link org.multiverse.api.exceptions.ControlFlowError} is reused. Normally you don't want to reuse them
     * because they can be expensive to create (especially the stacktrace) and they could be created very often. But for
     * debugging purposes it can be quite annoying because you want to see the stacktrace.
     *
     * @param reused true if ControlFlowErrors should be reused.
     * @return the updated TxnFactoryBuilder.
     * @see TxnConfig#isControlFlowErrorsReused()
     */
    TxnFactoryBuilder setControlFlowErrorsReused(boolean reused);

    /**
     * Sets the {@link Txn} familyname. If an {@link TxnExecutor} is used inside a method, a useful familyname could
     * be the full name of the class and the method.
     * <p/>
     * The transaction familyName is useful debugging purposes, but has not other meaning.
     *
     * @param familyName the familyName of the transaction.
     * @return the updated TxnFactoryBuilder
     * @throws NullPointerException if familyName is null.
     * @see TxnConfig#getFamilyName()
     */
    TxnFactoryBuilder setFamilyName(String familyName);

    /**
     * Sets the {@link org.multiverse.api.PropagationLevel} used. With the PropagationLevel you have control
     * on how the transaction deals with transaction nesting. The default is {@link PropagationLevel#Requires}
     * which automatically starts a transaction is one is missing, or lifts on a transaction if available.
     *
     * @param propagationLevel the new PropagationLevel
     * @return the updated TxnFactoryBuilder
     * @throws NullPointerException if propagationLevel is null.
     * @see TxnConfig#getPropagationLevel()
     * @see PropagationLevel
     */
    TxnFactoryBuilder setPropagationLevel(PropagationLevel propagationLevel);

    /**
     * Sets the {@link Txn} {@link LockMode} for all reads. If a LockMode is set higher than {@link LockMode#None}, this transaction
     * will locks all reads (and writes since a read is needed for a write) and the transaction automatically becomes
     * serialized.
     *
     * @param lockMode the LockMode to set.
     * @return the updated TxnFactoryBuilder.
     * @throws NullPointerException if lockMode is null.
     * @see TxnConfig#getReadLockMode()
     * @see LockMode
     */
    TxnFactoryBuilder setReadLockMode(LockMode lockMode);

    /**
     * Sets the {@link Txn} {@link LockMode} for all writes. For a write, always a read needs to be done, so if the read LockMode is
     *
     * <p>Freshly constructed objects that are not committed, automatically are locked with {@link LockMode#Exclusive}.
     *
     * <p>If the write LockMode is set after the read LockMode and the write LockMode is lower than the read LockMode,
     * an {@code IllegalTxnFactoryException} will be thrown when a {@link TxnFactory} is created.
     *
     * <p>If the write LockMode is set before the read LockMode and the write LockMode is lower than the read LockMode,
     * the write LockMode automatically is upgraded to that of the read LockMode. This makes setting the readLock
     * mode less of a nuisance.
     *
     * @param lockMode the LockMode to set.
     * @return the updated TxnFactoryBuilder.
     * @throws NullPointerException if lockMode is null.
     * @see TxnConfig#getWriteLockMode()
     * @see LockMode
     */
    TxnFactoryBuilder setWriteLockMode(LockMode lockMode);

    /**
     * Adds a permanent {@link Txn} {@link org.multiverse.api.lifecycle.TxnListener}. All permanent listeners are always executed after all normal
     * listeners are executed. If the same listener is added multiple times, it will be executed multiple times.
     *
     * <p>This method is very useful for integrating Multiverse in other JVM based environments because with this
     * approach you have a callback when transaction aborts/commit and can add your own logic. See the
     * {@link org.multiverse.api.lifecycle.TxnListener} for more information about normal vs permanent listeners.
     *
     * @param listener the permanent listener to add.
     * @return the updated TxnFactoryBuilder.
     * @throws NullPointerException if listener is null.
     * @see TxnConfig#getPermanentListeners()
     */
    TxnFactoryBuilder addPermanentListener(TxnListener listener);

    /**
     * Sets the {@link Txn} {@link TraceLevel}. With tracing it is possible to see what is happening inside a transaction.
     *
     * @param traceLevel the new traceLevel.
     * @return the updated TxnFactoryBuilder.
     * @throws NullPointerException if traceLevel is null.
     * @see TxnConfig#getTraceLevel()
     * @see TraceLevel
     */
    TxnFactoryBuilder setTraceLevel(TraceLevel traceLevel);

    /**
     * Sets the timeout (the maximum time a {@link Txn} is allowed to block. Long.MAX_VALUE indicates that an
     * unbound timeout should be used.
     *
     * @param timeoutNs the timeout specified in nano seconds
     * @return the updated TxnFactoryBuilder
     * @see TxnConfig#getTimeoutNs()
     * @see Txn#getRemainingTimeoutNs()
     */
    TxnFactoryBuilder setTimeoutNs(long timeoutNs);

    /**
     * Sets if the {@link Txn} can be interrupted while doing blocking operations.
     *
     * @param interruptible if the transaction can be interrupted while doing blocking operations.
     * @return the updated TxnFactoryBuilder
     * @see TxnConfig#isInterruptible()
     */
    TxnFactoryBuilder setInterruptible(boolean interruptible);

    /**
     * Sets the {@link Txn} {@link BackoffPolicy}. Policy is used to backoff when a transaction conflicts with another {@link Txn}.
     * See the {@link BackoffPolicy} for more information.
     *
     * @param backoffPolicy the backoff policy to use.
     * @return the updated TxnFactoryBuilder
     * @throws NullPointerException if backoffPolicy is null.
     * @see TxnConfig#getBackoffPolicy()
     */
    TxnFactoryBuilder setBackoffPolicy(BackoffPolicy backoffPolicy);

    /**
     * Sets if the {@link Txn} dirty check is enabled. Dirty check is that something only needs to be written,
     * if there really is a change (else it will be interpreted as a read). If it is disabled, it will always write, and
     * this could prevent the aba isolation anomaly, but causes more conflicts so more contention. In most cases enabling
     * it is the best option.
     *
     * @param dirtyCheckEnabled true if dirty check should be executed, false otherwise.
     * @return the updated TxnFactoryBuilder.
     * @see TxnConfig#isDirtyCheckEnabled()
     */
    TxnFactoryBuilder setDirtyCheckEnabled(boolean dirtyCheckEnabled);

    /**
     * Sets the maximum number of spins that are allowed when a {@link Txn} can't be read/written/locked
     * because it is locked by another transaction.
     *
     * <p>Setting the value to a very high value, could lead to more an increased chance of a live locking.
     *
     * @param spinCount the maximum number of spins
     * @return the updated TxnFactoryBuilder.
     * @throws IllegalArgumentException if spinCount smaller than 0.
     * @see TxnConfig#getSpinCount()
     */
    TxnFactoryBuilder setSpinCount(int spinCount);

    /**
     * Sets the readonly property on a {@link Txn}. If a transaction is configured as readonly, no write operations
     * (also no construction of new transactional objects making use of that transaction) is allowed
     *
     * @param readonly true if the transaction should be readonly, false otherwise.
     * @return the updated TxnFactoryBuilder
     * @see TxnConfig#isReadonly()
     */
    TxnFactoryBuilder setReadonly(boolean readonly);

    /**
     * Sets if the {@link Txn} should automatically track all reads that have been done. This is needed for blocking
     * operations, but also for other features like writeskew detection.
     *
     * <p>Tracking reads puts more pressure on the transaction since it needs to store all reads, but it reduces the chance
     * of read conflicts, since once read from main memory, it can be retrieved from the transaction.
     *
     * The transaction is free to track reads even though this property is disabled.
     *
     * @param enabled true if read tracking enabled, false otherwise.
     * @return the updated TxnFactoryBuilder
     * @see TxnConfig#isReadTrackingEnabled()
     */
    TxnFactoryBuilder setReadTrackingEnabled(boolean enabled);

    /**
     * With the speculative configuration enabled, the {@link Stm} is allowed to determine optimal settings for
     * a {@link Txn}.
     *
     * <p>Some behavior like readonly or the need for tracking reads can be determined runtime. The system can start with
     * a readonly non readtracking transaction and upgrade to an update or a read tracking once a write or retry
     * happens.
     *
     * <p>It depends on the {@link Stm} implementation on which properties it is going to speculate.
     *
     * <p>Enabling it can cause a few unexpected 'retries' of transactions, but it can seriously improve performance.
     *
     * @param speculative indicates if speculative configuration should be enabled.
     * @return the updated TxnFactoryBuilder
     * @see TxnConfig#isSpeculative()
     */
    TxnFactoryBuilder setSpeculative(boolean speculative);

    /**
     * Sets the the maximum count a {@link Txn} can be retried. The default is 1000. Setting it to a very low value
     * could mean that a transaction can't complete. Setting it to a very high value could lead to live-locking.
     *
     * <p>If the speculative configuration mechanism is enabled ({@link #setSpeculative(boolean)}), a few retries
     * are done in the beginning to figure out the best settings.
     *
     * @param maxRetries the maximum number of times a transaction can be tried.
     * @return the updated TxnFactoryBuilder
     * @throws IllegalArgumentException if maxRetries smaller than 0.
     * @see TxnConfig#getMaxRetries()
     */
    TxnFactoryBuilder setMaxRetries(int maxRetries);

    /**
     * Sets the {@link IsolationLevel} on the {@link Txn}.
     *
     * <p>The {@link Txn} is free to upgraded to a higher {@link IsolationLevel}. This is essentially the same
     * behavior you get when Oracle is used, where a read uncommitted is upgraded to a read committed and a repeatable
     * read is upgraded to the Oracle version of serialized (so with the writeskew problem still there).
     *
     * @param isolationLevel the new IsolationLevel
     * @return the updated TxnFactoryBuilder
     * @throws NullPointerException if isolationLevel is null.
     * @see TxnConfig#getIsolationLevel()
     * @see IsolationLevel
     */
    TxnFactoryBuilder setIsolationLevel(IsolationLevel isolationLevel);

    /**
     * Sets if the {@link Txn} is allowed to do an explicit retry (needed for a blocking operation). One use case
     * for disallowing it, it when the transaction is used inside an actor, and you don't want that inside the logic
     * executed by the agent a blocking operations is done (e.g. taking an item of a blocking queue).
     *
     * @param blockingAllowed true if explicit retry is allowed, false otherwise.
     * @return the updated TxnFactoryBuilder
     */
    TxnFactoryBuilder setBlockingAllowed(boolean blockingAllowed);

    /**
     * Builds a new {@link TxnFactory}.
     *
     * @return the build TxnFactory.
     * @throws org.multiverse.api.exceptions.IllegalTxnFactoryException
     *          if the TxnFactory could not be build
     *          because the configuration was not correct.
     */
    TxnFactory newTransactionFactory();

    /**
     * Builds a new {@link TxnExecutor} optimized for executing transactions created by this TxnFactoryBuilder.
     *
     * @return the created TxnExecutor.
     * @throws org.multiverse.api.exceptions.IllegalTxnFactoryException
     *          if the TxnFactory could not be build
     *          because the configuration was not correct.
     */
    TxnExecutor newTxnExecutor();
}
