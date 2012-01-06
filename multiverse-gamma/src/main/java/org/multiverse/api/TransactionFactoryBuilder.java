package org.multiverse.api;

import org.multiverse.api.lifecycle.TransactionListener;

/**
 * A Builder for creating a {@link TransactionFactory} and {@link AtomicBlock}. This builder provides full control
 * on transaction settings.
 *
 * <p>Since the {@link Transaction} and {@link AtomicBlock} are very closely integrated, both of them are created
 * by this TransactionFactoryBuilder.
 *
 * <p>Instances of this class are considered immutable, so when you call one of the modifying methods, make sure
 * that you use the resulting TransactionFactoryBuilder. Normally with the builder implementation the same
 * instance is returned. In this case this isn't true because a new instance is returned every time.
 *
 * @author Peter Veentjer
 * @see TransactionFactory
 * @see TransactionConfiguration
 */
public interface TransactionFactoryBuilder {

    /**
     * Returns the {@link }TransactionConfiguration} used by this TransactionFactoryBuilder.
     *
     * @return the used TransactionConfiguration.
     */
    TransactionConfiguration getConfiguration();

    /**
     * Sets if the {@link org.multiverse.api.exceptions.ControlFlowError} is reused. Normally you don't want to reuse them
     * because they can be expensive to create (especially the stacktrace) and they could be created very often. But for
     * debugging purposes it can be quite annoying because you want to see the stacktrace.
     *
     * @param reused true if ControlFlowErrors should be reused.
     * @return the updated TransactionFactoryBuilder.
     * @see TransactionConfiguration#isControlFlowErrorsReused()
     */
    TransactionFactoryBuilder setControlFlowErrorsReused(boolean reused);

    /**
     * Sets the {@link Transaction} familyname. If an {@link AtomicBlock} is used inside a method, a useful familyname could
     * be the full name of the class and the method.
     * <p/>
     * The transaction familyName is useful debugging purposes, but has not other meaning.
     *
     * @param familyName the familyName of the transaction.
     * @return the updated TransactionFactoryBuilder
     * @throws NullPointerException if familyName is null.
     * @see TransactionConfiguration#getFamilyName()
     */
    TransactionFactoryBuilder setFamilyName(String familyName);

    /**
     * Sets the {@link org.multiverse.api.PropagationLevel} used. With the PropagationLevel you have control
     * on how the transaction deals with transaction nesting. The default is {@link PropagationLevel#Requires}
     * which automatically starts a transaction is one is missing, or lifts on a transaction if available.
     *
     * @param propagationLevel the new PropagationLevel
     * @return the updated TransactionFactoryBuilder
     * @throws NullPointerException if propagationLevel is null.
     * @see TransactionConfiguration#getPropagationLevel()
     * @see PropagationLevel
     */
    TransactionFactoryBuilder setPropagationLevel(PropagationLevel propagationLevel);

    /**
     * Sets the {@link Transaction} {@link LockMode} for all reads. If a LockMode is set higher than {@link LockMode#None}, this transaction
     * will locks all reads (and writes since a read is needed for a write) and the transaction automatically becomes
     * serialized.
     *
     * @param lockMode the LockMode to set.
     * @return the updated TransactionFactoryBuilder.
     * @throws NullPointerException if lockMode is null.
     * @see TransactionConfiguration#getReadLockMode()
     * @see LockMode
     */
    TransactionFactoryBuilder setReadLockMode(LockMode lockMode);

    /**
     * Sets the {@link Transaction{} {@link LockMode} for all writes. For a write, always a read needs to be done, so if the read LockMode is
     *
     * <p>Freshly constructed objects that are not committed, automatically are locked with {@link LockMode#Exclusive}.
     *
     * <p>If the write LockMode is set after the read LockMode and the write LockMode is lower than the read LockMode,
     * an {@code IllegalTransactionFactoryException} will be thrown when a {@link TransactionFactory} is created.
     *
     * <p>If the write LockMode is set before the read LockMode and the write LockMode is lower than the read LockMode,
     * the write LockMode automatically is upgraded to that of the read LockMode. This makes setting the readLock
     * mode less of a nuisance.
     *
     * @param lockMode the LockMode to set.
     * @return the updated TransactionFactoryBuilder.
     * @throws NullPointerException if lockMode is null.
     * @see TransactionConfiguration#getWriteLockMode()
     * @see LockMode
     */
    TransactionFactoryBuilder setWriteLockMode(LockMode lockMode);

    /**
     * Adds a permanent {@link Transaction} {@link TransactionListener}. All permanent listeners are always executed after all normal
     * listeners are executed. If the same listener is added multiple times, it will be executed multiple times.
     *
     * <p>This method is very useful for integrating Multiverse in other JVM based environments because with this
     * approach you have a callback when transaction aborts/commit and can add your own logic. See the
     * {@link TransactionListener} for more information about normal vs permanent listeners.
     *
     * @param listener the permanent listener to add.
     * @return the updated TransactionFactoryBuilder.
     * @throws NullPointerException if listener is null.
     * @see TransactionConfiguration#getPermanentListeners()
     */
    TransactionFactoryBuilder addPermanentListener(TransactionListener listener);

    /**
     * Sets the {@link Transaction} {@link TraceLevel}. With tracing it is possible to see what is happening inside a transaction.
     *
     * @param traceLevel the new traceLevel.
     * @return the updated TransactionFactoryBuilder.
     * @throws NullPointerException if traceLevel is null.
     * @see TransactionConfiguration#getTraceLevel()
     * @see TraceLevel
     */
    TransactionFactoryBuilder setTraceLevel(TraceLevel traceLevel);

    /**
     * Sets the timeout (the maximum time a {@link Transaction} is allowed to block. Long.MAX_VALUE indicates that an
     * unbound timeout should be used.
     *
     * @param timeoutNs the timeout specified in nano seconds
     * @return the updated TransactionFactoryBuilder
     * @see TransactionConfiguration#getTimeoutNs()
     * @see Transaction#getRemainingTimeoutNs()
     */
    TransactionFactoryBuilder setTimeoutNs(long timeoutNs);

    /**
     * Sets if the {@link Transaction} can be interrupted while doing blocking operations.
     *
     * @param interruptible if the transaction can be interrupted while doing blocking operations.
     * @return the updated TransactionFactoryBuilder
     * @see TransactionConfiguration#isInterruptible()
     */
    TransactionFactoryBuilder setInterruptible(boolean interruptible);

    /**
     * Sets the {@link Transaction} {@link BackoffPolicy}. Policy is used to backoff when a transaction conflicts with another {@link Transaction}.
     * See the {@link BackoffPolicy} for more information.
     *
     * @param backoffPolicy the backoff policy to use.
     * @return the updated TransactionFactoryBuilder
     * @throws NullPointerException if backoffPolicy is null.
     * @see TransactionConfiguration#getBackoffPolicy()
     */
    TransactionFactoryBuilder setBackoffPolicy(BackoffPolicy backoffPolicy);

    /**
     * Sets if the {@link Transaction} dirty check is enabled. Dirty check is that something only needs to be written,
     * if there really is a change (else it will be interpreted as a read). If it is disabled, it will always write, and
     * this could prevent the aba isolation anomaly, but causes more conflicts so more contention. In most cases enabling
     * it is the best option.
     *
     * @param dirtyCheckEnabled true if dirty check should be executed, false otherwise.
     * @return the updated TransactionFactoryBuilder.
     * @see TransactionConfiguration#isDirtyCheckEnabled()
     */
    TransactionFactoryBuilder setDirtyCheckEnabled(boolean dirtyCheckEnabled);

    /**
     * Sets the maximum number of spins that are allowed when a {@link Transaction} can't be read/written/locked
     * because it is locked by another transaction.
     *
     * <p>Setting the value to a very high value, could lead to more an increased chance of a live locking.
     *
     * @param spinCount the maximum number of spins
     * @return the updated TransactionFactoryBuilder.
     * @throws IllegalArgumentException if spinCount smaller than 0.
     * @see TransactionConfiguration#getSpinCount()
     */
    TransactionFactoryBuilder setSpinCount(int spinCount);

    /**
     * Sets the readonly property on a {@link Transaction}. If a transaction is configured as readonly, no write operations
     * (also no construction of new transactional objects making use of that transaction) is allowed
     *
     * @param readonly true if the transaction should be readonly, false otherwise.
     * @return the updated TransactionFactoryBuilder
     * @see TransactionConfiguration#isReadonly()
     */
    TransactionFactoryBuilder setReadonly(boolean readonly);

    /**
     * Sets if the {@link Transaction} should automatically track all reads that have been done. This is needed for blocking
     * operations, but also for other features like writeskew detection.
     *
     * <p>Tracking reads puts more pressure on the transaction since it needs to store all reads, but it reduces the chance
     * of read conflicts, since once read from main memory, it can be retrieved from the transaction.
     *
     * The transaction is free to track reads even though this property is disabled.
     *
     * @param enabled true if read tracking enabled, false otherwise.
     * @return the updated TransactionFactoryBuilder
     * @see TransactionConfiguration#isReadTrackingEnabled()
     */
    TransactionFactoryBuilder setReadTrackingEnabled(boolean enabled);

    /**
     * With the speculative configuration enabled, the {@link Stm} is allowed to determine optimal settings for
     * a {@link Transaction}.
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
     * @return the updated TransactionFactoryBuilder
     * @see TransactionConfiguration#isSpeculative()
     */
    TransactionFactoryBuilder setSpeculative(boolean speculative);

    /**
     * Sets the the maximum count a {@link Transaction} can be retried. The default is 1000. Setting it to a very low value
     * could mean that a transaction can't complete. Setting it to a very high value could lead to live-locking.
     *
     * <p>If the speculative configuration mechanism is enabled ({@link #setSpeculative(boolean)}), a few retries
     * are done in the beginning to figure out the best settings.
     *
     * @param maxRetries the maximum number of times a transaction can be tried.
     * @return the updated TransactionFactoryBuilder
     * @throws IllegalArgumentException if maxRetries smaller than 0.
     * @see TransactionConfiguration#getMaxRetries()
     */
    TransactionFactoryBuilder setMaxRetries(int maxRetries);

    /**
     * Sets the {@link IsolationLevel} on the {@link Transaction}.
     *
     * <p>The {@link Transaction} is free to upgraded to a higher {@link IsolationLevel}. This is essentially the same
     * behavior you get when Oracle is used, where a read uncommitted is upgraded to a read committed and a repeatable
     * read is upgraded to the Oracle version of serialized (so with the writeskew problem still there).
     *
     * @param isolationLevel the new IsolationLevel
     * @return the updated TransactionFactoryBuilder
     * @throws NullPointerException if isolationLevel is null.
     * @see TransactionConfiguration#getIsolationLevel()
     * @see IsolationLevel
     */
    TransactionFactoryBuilder setIsolationLevel(IsolationLevel isolationLevel);

    /**
     * Sets if the {@link Transaction} is allowed to do an explicit retry (needed for a blocking operation). One use case
     * for disallowing it, it when the transaction is used inside an actor, and you don't want that inside the logic
     * executed by the agent a blocking operations is done (e.g. taking an item of a blocking queue).
     *
     * @param blockingAllowed true if explicit retry is allowed, false otherwise.
     * @return the updated TransactionFactoryBuilder
     */
    TransactionFactoryBuilder setBlockingAllowed(boolean blockingAllowed);

    /**
     * Builds a new {@link TransactionFactory}.
     *
     * @return the build TransactionFactory.
     * @throws org.multiverse.api.exceptions.IllegalTransactionFactoryException
     *          if the TransactionFactory could not be build
     *          because the configuration was not correct.
     */
    TransactionFactory newTransactionFactory();

    /**
     * Builds a new {@link AtomicBlock} optimized for executing transactions created by this TransactionFactoryBuilder.
     *
     * @return the created AtomicBlock.
     * @throws org.multiverse.api.exceptions.IllegalTransactionFactoryException
     *          if the TransactionFactory could not be build
     *          because the configuration was not correct.
     */
    AtomicBlock newAtomicBlock();
}
