package org.multiverse.stms.gamma;

import org.multiverse.api.BackoffPolicy;
import org.multiverse.api.DefaultBackoffPolicy;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.api.PropagationLevel;
import org.multiverse.api.TraceLevel;
import org.multiverse.api.lifecycle.TransactionListener;

import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

/**
 * Contains the default configuration for all transactions created by the GammaStm. With the TxnFactoryBuilder,
 * this behavior can be overridden.
 * <p/>
 * Once the GammaStm has been created, changes on this structure are ignored because the content of this configuration
 * is copied.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"ClassWithTooManyFields"})
public final class GammaStmConfiguration {

    /**
     * Contains the permanent TransactionListeners that should always be executed. Null references are not allowed.
     */
    public List<TransactionListener> permanentListeners = new LinkedList<TransactionListener>();

    /**
     * The default propagation level for all transactions executed by the Stm.
     */
    public PropagationLevel propagationLevel = PropagationLevel.Requires;

    /**
     * The default isolation level for all transactions executed by the GammaStm.
     */
    public IsolationLevel isolationLevel = IsolationLevel.Snapshot;

    /**
     * The default isolation level for all reads. Putting this to a level higher than LockMode.None has the same
     * effect as putting the isolationLevel to IsolationLevel.Serialized although the former approach will be
     * pessimistic and the later be optimistic.
     */
    public LockMode readLockMode = LockMode.None;

    /**
     * The default isolation level for all writes. For a write also a read is acquired, so the highest one wins.
     * <p/>
     * Putting it the LockMode.Write gives the same behavior as you have with Oracle. Reads are still allowed (even
     * though a WriteLock is allowed) only other transactions are not able to acquire a lock on it (whatever the lock
     * should be).
     */
    public LockMode writeLockMode = LockMode.None;

    /**
     * The default behavior if blocking transactions are allowed.
     */
    public boolean blockingAllowed = true;

    /**
     * The default behavior for blocking transactions if they are allowed to be interrupted.
     */
    public boolean interruptible = false;

    /**
     * The default timeout for a transaction if it blocks. A Long.MAX_VALUE indicates that there is no timeout.
     */
    public long timeoutNs = Long.MAX_VALUE;

    /**
     * The default readonly behavior. Setting this to true would be quite useless.
     */
    public boolean readonly = false;

    /**
     * The default number of spins a transaction is allowed for a read/write/commit if something is locked.
     */
    public int spinCount = 64;

    /**
     * The default behavior for writing 'dirty' changes for an update transaction. If it is set to true, a change needs to
     * be made. If there is no change, it will not be written (and essentially be seen as a read).
     */
    public boolean dirtyCheck = true;

    /**
     * The minimal size for the internal array for a variable length transaction. A variable length transaction internally uses
     * an array to store its content and when the transaction grows, the array will grow accordingly.
     */
    public int minimalVariableLengthTransactionSize = 4;

    /**
     * If reads should be tracked by the transaction (this is something else than (semi)visible reads). The advantage of readtracking
     * is there is less overhead once it has been read, but the disadvantage is that the transaction needs to track more changes.
     */
    public boolean trackReads = true;

    /**
     * The default number of retries a transaction is allowed to do if a transaction fails for a read/write conflict. The GammaStm also
     * uses a speculative configuration mechanism that can consume some, so setting it to a very low value in combination with
     * speculativeConfigEnabled is not recommended.
     */
    public int maxRetries = 1000;

    /**
     * The GammaStm makes use of a speculative mechanism to select the optimal transaction settings/implementation for some atomicChecked block.
     * An TransactionExecutor will start cheap and grow to use more expensive transaction implementations (see the Lean/Fat transactions) and
     * use the automatic retry mechanism of the STM to rescue itself from a situation where the speculation was incorrect. Setting it to false
     * reduces the number of unwanted conflicts (once the TransactionExecutor has learned it will not make the same mistakes again, so you only need
     * to pay the price of speculation in the beginning) at the cost of overhead.
     */
    public boolean speculativeConfigEnabled = true;

    /**
     * The maximum size size of a fixed length transaction. A fixed length transaction is very cheap compared to a variable length, but
     * the big problem of the fixed length is that it needs to do a full transaction scan to see if the desired data is there. So there is
     * a point where the full scan gets more expensive (O(N) vs O(log n)) expensive.
     * <p/>
     * Atm there has not been put much research in finding the optimal size and it could differ from machine to machine.
     * <p/>
     * It also is important that the fixed length transactions are able to put a frequently read ref in a hot spot, making the overhead
     * of searching lower.
     */
    public int maxFixedLengthTransactionSize = 20;

    /**
     * If a transaction fails for a read/write conflict it should not hammer the system by trying again and running in the same conflict
     * The default backoff policy helps to back threads of by sleeping/yielding.
     */
    public BackoffPolicy backoffPolicy = DefaultBackoffPolicy.MAX_100_MS;

    /**
     * With the trace level you have control if you get output of transactions executing. It helps with debugging. If the
     * org.multiverse.MultiverseConstants.___TracingEnabled is not set to true, this value is ignored and the whole profiling
     * stuff is removed by the JIT since it effectively has become dead code.
     */
    public TraceLevel traceLevel = TraceLevel.None;

    /**
     * If control flow errors should be reused. Normally exception reuse would be a very very very bad thing to do. But if they are
     * used to regulate control flow, they can be thrown thousands of times a second and this puts a lot of pressure on the gc. The most
     * expensive part is building the StackTrace.
     * <p/>
     * For more info about the control flow errors see the subclasses of the {@link org.multiverse.api.exceptions.ControlFlowError} like
     * the {@link org.multiverse.api.exceptions.ReadWriteConflict}, {@link org.multiverse.api.exceptions.RetryError} and the
     * {@link org.multiverse.api.exceptions.SpeculativeConfigurationError}.
     */
    public boolean controlFlowErrorsReused = true;

    /**
     * Should only be used internally to select fat instead of lean transactions. Normally the speculative configuration takes care of this
     * but for testing purposes you want to control it manually.
     */
    public boolean isFat = false;

    /**
     * The maximum size of a transaction that is allowed to do a full conflict scan instead of arrive/depart operations. Arrive/depart
     * is more expensive since it increased the pressure on refs that a full conflict scan for short transactions, but at a certain length
     * of the transaction only needing to do a full conflict scan when the global conflict counter increases, becomes cheaper.
     */
    public int maximumPoorMansConflictScanLength = 20;

    /**
     * The number of times a transactional object is only read before becoming readbiased. The advantage of a readBiased transactional object
     * is that you don't need to arrive/depart (richmans conflict scan), but the disadvantage is that it could cause transactions to do
     * full conflict scans even though they are not required.
     */
    public int readBiasedThreshold = 128;

    /**
     * Checks if the configuration is valid.
     *
     * @throws IllegalStateException if the configuration isn't valid.
     */
    public void validate() {
        if (timeoutNs < 0) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] timeoutNs can't be smaller than 0, " +
                            "timeoutNs was " + timeoutNs);
        }

        if (readBiasedThreshold < 0) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] readBiasedThreshold can't be smaller than 0, " +
                            "readBiasedThreshold was " + readBiasedThreshold);
        }

        if (readBiasedThreshold > 1023) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] readBiasedThreshold can't be larger than 1023, " +
                            "readBiasedThreshold was " + readBiasedThreshold);
        }

        if (maximumPoorMansConflictScanLength < 0) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] maximumFullConflictScanSize can't be smaller than 0, " +
                            "maximumFullConflictScanSize was " + maxFixedLengthTransactionSize);
        }

        if (readLockMode == null) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] readLockMode can't be null");
        }

        if (writeLockMode == null) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] writeLockMode can't be null");
        }

        if (writeLockMode.asInt() < readLockMode.asInt()) {
            throw new IllegalStateException(
                    format("[GammaStmConfiguration] writeLockMode [%s] can't be lower than readLockMode [%s]",
                            writeLockMode, readLockMode));
        }

        if (isolationLevel == null) {
            throw new IllegalStateException("[GammaStmConfiguration] isolationLevel can't be null");
        }

        if (isolationLevel.doesAllowWriteSkew() && !trackReads) {
            throw new IllegalStateException(
                    format("[GammaStmConfiguration] isolation level '%s' can't be combined with readtracking" +
                            "is false since it is needed to prevent the writeskew problem", isolationLevel));
        }

        if (blockingAllowed && !trackReads) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] blockingAllowed can't be true if trackReads is false");
        }

        if (spinCount < 0) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] spinCount can't be smaller than 0, but was " + spinCount);
        }

        if (minimalVariableLengthTransactionSize < 1) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] minimalVariableLengthTransactionSize can't be smaller than 1, but was "
                            + minimalVariableLengthTransactionSize);
        }

        if (maxRetries < 0) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] maxRetries can't be smaller than 0, but was " + maxRetries);
        }

        if (maxFixedLengthTransactionSize < 2) {
            throw new IllegalStateException(
                    "[GammaStmConfiguration] maxFixedLengthTransactionSize can't be smaller than 2, but was "
                            + maxFixedLengthTransactionSize);
        }

        if (backoffPolicy == null) {
            throw new IllegalStateException("[GammaStmConfiguration] backoffPolicy can't be null");
        }

        if (traceLevel == null) {
            throw new IllegalStateException("[GammaStmConfiguration] traceLevel can't be null");
        }

        if (propagationLevel == null) {
            throw new IllegalStateException("[GammaStmConfiguration] propagationLevel can't be null");
        }

        if (permanentListeners == null) {
            throw new IllegalStateException("[GammaStmConfiguration] permanentListeners can't be null");
        }

        for (int k = 0; k < permanentListeners.size(); k++) {
            TransactionListener listener = permanentListeners.get(k);
            if (listener == null) {
                throw new IllegalStateException(
                        format("[GammaStmConfiguration] permanentListener at index %s can't be null", k));
            }
        }
    }
}
