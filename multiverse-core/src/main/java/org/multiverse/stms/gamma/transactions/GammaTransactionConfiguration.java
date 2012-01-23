package org.multiverse.stms.gamma.transactions;

import org.multiverse.api.BackoffPolicy;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.api.PropagationLevel;
import org.multiverse.api.TraceLevel;
import org.multiverse.api.TransactionConfiguration;
import org.multiverse.api.exceptions.IllegalTransactionFactoryException;
import org.multiverse.api.lifecycle.TransactionListener;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.GlobalConflictCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.unmodifiableList;

/**
 * A configuration object that contains the configuration for a GammaTransaction.
 * <p/>
 * GammaTransactionConfiguration object is considered to be immutable. The only mutable part if the speculative
 * configuration that can get upgraded if enabled and speculations failed.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"OverlyComplexClass", "ClassWithTooManyFields"})
public final class GammaTransactionConfiguration implements TransactionConfiguration, GammaConstants {

    public final static AtomicLong idGenerator = new AtomicLong();
    public final AtomicReference<SpeculativeGammaConfiguration> speculativeConfiguration
            = new AtomicReference<SpeculativeGammaConfiguration>();

    public final GammaStm stm;
    public final GlobalConflictCounter globalConflictCounter;
    public PropagationLevel propagationLevel;
    public IsolationLevel isolationLevel;
    public boolean writeSkewAllowed;
    public boolean inconsistentReadAllowed;
    public LockMode readLockMode;
    public LockMode writeLockMode;
    public int readLockModeAsInt;
    public int writeLockModeAsInt;
    public String familyName;
    public boolean isAnonymous;
    public boolean interruptible;
    public boolean readonly;
    public int spinCount;
    public boolean dirtyCheck;
    public int minimalArrayTreeSize;
    public boolean trackReads;
    public boolean blockingAllowed;
    public int maxRetries;
    public boolean speculative;
    public int maxFixedLengthTransactionSize;
    public BackoffPolicy backoffPolicy;
    public long timeoutNs;
    public TraceLevel traceLevel;
    public boolean controlFlowErrorsReused;
    public boolean isFat;
    public int maximumPoorMansConflictScanLength;
    public ArrayList<TransactionListener> permanentListeners;
    public boolean unrepeatableReadAllowed;

    public GammaTransactionConfiguration(GammaStm stm) {
        this(stm, new GammaStmConfiguration());
    }

    public GammaTransactionConfiguration(GammaStm stm, GammaStmConfiguration config) {
        this.stm = stm;
        this.globalConflictCounter = stm.getGlobalConflictCounter();
        this.interruptible = config.interruptible;
        this.readonly = config.readonly;
        this.spinCount = config.spinCount;
        this.readLockMode = config.readLockMode;
        this.readLockModeAsInt = config.readLockMode.asInt();
        this.writeLockMode = config.writeLockMode;
        this.writeLockModeAsInt = config.writeLockMode.asInt();
        this.dirtyCheck = config.dirtyCheck;
        this.minimalArrayTreeSize = config.minimalVariableLengthTransactionSize;
        this.trackReads = config.trackReads;
        this.blockingAllowed = config.blockingAllowed;
        this.maxRetries = config.maxRetries;
        this.speculative = config.speculativeConfigEnabled;
        this.maxFixedLengthTransactionSize = config.maxFixedLengthTransactionSize;
        this.backoffPolicy = config.backoffPolicy;
        this.timeoutNs = config.timeoutNs;
        this.traceLevel = config.traceLevel;
        this.isolationLevel = config.isolationLevel;
        this.writeSkewAllowed = isolationLevel.doesAllowWriteSkew();
        this.inconsistentReadAllowed = isolationLevel.doesAllowInconsistentRead();
        this.unrepeatableReadAllowed = isolationLevel.doesAllowUnrepeatableRead();
        this.propagationLevel = config.propagationLevel;
        this.controlFlowErrorsReused = config.controlFlowErrorsReused;
        this.familyName = "anonymoustransaction-" + idGenerator.incrementAndGet();
        this.isAnonymous = true;
        this.maximumPoorMansConflictScanLength = config.maximumPoorMansConflictScanLength;
        this.isFat = config.isFat;
        if (config.permanentListeners.isEmpty()) {
            this.permanentListeners = null;
        } else {
            this.permanentListeners = new ArrayList<TransactionListener>(config.permanentListeners);
        }
    }

    /**
     * Makes a clone of the given GammaTransactionConfiguration.
     *
     * @param config the GammaTransactionConfiguration to clone.
     */
    private GammaTransactionConfiguration(GammaTransactionConfiguration config) {
        this.stm = config.stm;
        this.globalConflictCounter = config.globalConflictCounter;
        this.propagationLevel = config.propagationLevel;
        this.isolationLevel = config.isolationLevel;
        this.writeSkewAllowed = config.writeSkewAllowed;
        this.inconsistentReadAllowed = config.inconsistentReadAllowed;
        this.readLockMode = config.readLockMode;
        this.readLockModeAsInt = config.readLockModeAsInt;
        this.writeLockMode = config.writeLockMode;
        this.writeLockModeAsInt = config.writeLockModeAsInt;
        this.familyName = config.familyName;
        this.isAnonymous = config.isAnonymous;
        this.interruptible = config.interruptible;
        this.readonly = config.readonly;
        this.spinCount = config.spinCount;
        this.dirtyCheck = config.dirtyCheck;
        this.minimalArrayTreeSize = config.minimalArrayTreeSize;
        this.trackReads = config.trackReads;
        this.blockingAllowed = config.blockingAllowed;
        this.maxRetries = config.maxRetries;
        this.speculative = config.speculative;
        this.maxFixedLengthTransactionSize = config.maxFixedLengthTransactionSize;
        this.backoffPolicy = config.backoffPolicy;
        this.timeoutNs = config.timeoutNs;
        this.traceLevel = config.traceLevel;
        this.controlFlowErrorsReused = config.controlFlowErrorsReused;
        this.isFat = config.isFat;
        this.maximumPoorMansConflictScanLength = config.maximumPoorMansConflictScanLength;
        this.permanentListeners = config.permanentListeners;
    }

    public GammaTransactionConfiguration(GammaStm stm, int maxFixedLengthTransactionSize) {
        this(stm);
        this.maxFixedLengthTransactionSize = maxFixedLengthTransactionSize;
    }

    @Override
    public LockMode getReadLockMode() {
        return readLockMode;
    }

    @Override
    public LockMode getWriteLockMode() {
        return writeLockMode;
    }

    @Override
    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    @Override
    public boolean isControlFlowErrorsReused() {
        return controlFlowErrorsReused;
    }

    public SpeculativeGammaConfiguration getSpeculativeConfiguration() {
        return speculativeConfiguration.get();
    }

    @Override
    public long getTimeoutNs() {
        return timeoutNs;
    }

    @Override
    public TraceLevel getTraceLevel() {
        return traceLevel;
    }

    @Override
    public boolean isInterruptible() {
        return interruptible;
    }

    @Override
    public BackoffPolicy getBackoffPolicy() {
        return backoffPolicy;
    }

    @Override
    public boolean isSpeculative() {
        return speculative;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public int getSpinCount() {
        return spinCount;
    }

    @Override
    public boolean isDirtyCheckEnabled() {
        return dirtyCheck;
    }

    @Override
    public GammaStm getStm() {
        return stm;
    }

    public GlobalConflictCounter getGlobalConflictCounter() {
        return globalConflictCounter;
    }

    @Override
    public boolean isReadTrackingEnabled() {
        return trackReads;
    }

    @Override
    public boolean isBlockingAllowed() {
        return blockingAllowed;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public PropagationLevel getPropagationLevel() {
        return propagationLevel;
    }

    @Override
    public List<TransactionListener> getPermanentListeners() {
        if (permanentListeners == null) {
            return EMPTY_LIST;
        }
        return unmodifiableList(permanentListeners);
    }

    public void updateSpeculativeConfigurationToUseNonRefType() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration update = current.newWithNonRefType();
            if (speculativeConfiguration.compareAndSet(current, update)) {
                return;
            }
        }
    }

    public void updateSpeculativeConfigurationToUseListeners() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration update = current.newWithListeners();
            if (speculativeConfiguration.compareAndSet(current, update)) {
                return;
            }
        }
    }

    public void updateSpeculativeConfigureToUseAbortOnly() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration update = current.newWithAbortOnly();
            if (speculativeConfiguration.compareAndSet(current, update)) {
                return;
            }
        }
    }

    public void updateSpeculativeConfigurationToUseCommute() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration update = current.newWithCommute();
            if (speculativeConfiguration.compareAndSet(current, update)) {
                return;
            }
        }
    }

    public void updateSpeculativeConfigurationToUseExplicitLocking() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration update = current.newWithLocks();
            if (speculativeConfiguration.compareAndSet(current, update)) {
                return;
            }
        }
    }

    public void updateSpeculativeConfigurationToUseConstructedObjects() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration next = current.newWithConstructedObjects();

            if (speculativeConfiguration.compareAndSet(current, next)) {
                return;
            }
        }
    }

    public void updateSpeculativeConfigurationToUseRichMansConflictScan() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration next = current.newWithRichMansConflictScan();

            if (speculativeConfiguration.compareAndSet(current, next)) {
                return;
            }
        }
    }

    public void updateSpeculativeConfigurationToUseMinimalTransactionLength(int newLength) {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration next = current.newWithMinimalLength(newLength);

            if (speculativeConfiguration.compareAndSet(current, next)) {
                return;
            }
        }
    }


    public void updateSpeculativeConfigurationToUseEnsure() {
        while (true) {
            SpeculativeGammaConfiguration current = speculativeConfiguration.get();
            SpeculativeGammaConfiguration next = current.newWithEnsure();

            if (speculativeConfiguration.compareAndSet(current, next)) {
                return;
            }
        }
    }

    public GammaTransactionConfiguration init() {
        if (!writeSkewAllowed && !trackReads && !readonly) {
            String msg = format("'[%s] If no writeskew is allowed, read tracking should be enabled", familyName);
            throw new IllegalTransactionFactoryException(msg);
        }

        if (blockingAllowed && !trackReads) {
            String msg = format("[%s] If blocking is allowed, read tracking should be enabled", familyName);
            throw new IllegalTransactionFactoryException(msg);
        }

        if (readLockModeAsInt > writeLockModeAsInt) {
            String msg = format("[%s] The used write LockMode [%s] should be equal or higher than the read LockMode [%s]",
                    familyName, readLockMode, writeLockMode);
            throw new IllegalTransactionFactoryException(msg);
        }

        if (readLockMode != LockMode.None && !trackReads) {
            String msg = format("[%s] If readLockMode is [%s] , read tracking should be enabled",
                    familyName, readLockMode);
            throw new IllegalTransactionFactoryException(msg);
        }

        if (speculativeConfiguration.get() == null) {
            SpeculativeGammaConfiguration newSpeculativeConfiguration;
            if (speculative) {

                newSpeculativeConfiguration = new SpeculativeGammaConfiguration(
                        isFat(), false, false, false, false, false, false, false, false, false, 1);
            } else {
                newSpeculativeConfiguration = new SpeculativeGammaConfiguration(
                        true, true, true, true, true, true, true, true, true, true, Integer.MAX_VALUE);
            }

            if (maximumPoorMansConflictScanLength == 0) {
                newSpeculativeConfiguration = newSpeculativeConfiguration.newWithRichMansConflictScan();
            }

            speculativeConfiguration.compareAndSet(null, newSpeculativeConfiguration);
        }

        return this;
    }

    private boolean isFat() {
        if (isFat) {
            return true;
        }

        if (isolationLevel != IsolationLevel.Snapshot) {
            return true;
        }

        if (permanentListeners != null) {
            return true;
        }

        if (readLockMode != LockMode.None) {
            return true;
        }

        if (writeLockMode != LockMode.None) {
            return true;
        }

        if (dirtyCheck) {
            return true;
        }

        if (readonly) {
            return true;
        }

        return false;
    }

    public GammaTransactionConfiguration setTimeoutNs(long timeoutNs) {
        if (timeoutNs < 0) {
            throw new IllegalArgumentException("timeoutNs can't be smaller than 0");
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.timeoutNs = timeoutNs;
        return config;
    }

    public GammaTransactionConfiguration setFamilyName(String familyName) {
        if (familyName == null) {
            throw new NullPointerException("familyName can't be null");
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.isAnonymous = false;
        config.familyName = familyName;
        return config;
    }

    public GammaTransactionConfiguration setMaxRetries(int maxRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries can't be smaller than 0");
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.maxRetries = maxRetries;
        return config;
    }

    public GammaTransactionConfiguration setMaximumPoorMansConflictScanLength(int maximumPoorMansConflictScanLength) {
        if (maximumPoorMansConflictScanLength < 0) {
            throw new IllegalStateException();
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.maximumPoorMansConflictScanLength = maximumPoorMansConflictScanLength;
        return config;
    }

    public GammaTransactionConfiguration setReadTrackingEnabled(boolean trackReads) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.trackReads = trackReads;
        return config;
    }

    public GammaTransactionConfiguration setSpeculative(boolean speculativeConfigEnabled) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.speculative = speculativeConfigEnabled;
        return config;
    }

    public GammaTransactionConfiguration setReadonly(boolean readonly) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.readonly = readonly;
        return config;
    }

    public GammaTransactionConfiguration setDirtyCheckEnabled(boolean dirtyCheck) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.dirtyCheck = dirtyCheck;
        return config;
    }

    public GammaTransactionConfiguration setBlockingAllowed(boolean blockingAllowed) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.blockingAllowed = blockingAllowed;
        return config;
    }

    public GammaTransactionConfiguration setInterruptible(boolean interruptible) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.interruptible = interruptible;
        return config;
    }

    public GammaTransactionConfiguration setControlFlowErrorsReused(boolean controlFlowErrorsReused) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.controlFlowErrorsReused = controlFlowErrorsReused;
        return config;
    }

    public GammaTransactionConfiguration setSpinCount(int spinCount) {
        if (spinCount < 0) {
            throw new IllegalArgumentException("spinCount can't be smaller than 0");
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.spinCount = spinCount;
        return config;
    }

    public GammaTransactionConfiguration setBackoffPolicy(BackoffPolicy backoffPolicy) {
        if (backoffPolicy == null) {
            throw new NullPointerException("backoffPolicy can't be null");
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.backoffPolicy = backoffPolicy;
        return config;
    }

    public GammaTransactionConfiguration setTraceLevel(TraceLevel traceLevel) {
        if (traceLevel == null) {
            throw new NullPointerException("traceLevel can't be null");
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.traceLevel = traceLevel;
        return config;
    }

    public GammaTransactionConfiguration setPropagationLevel(PropagationLevel propagationLevel) {
        if (propagationLevel == null) {
            throw new NullPointerException();
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.propagationLevel = propagationLevel;
        return config;
    }


    public GammaTransactionConfiguration setIsolationLevel(IsolationLevel isolationLevel) {
        if (isolationLevel == null) {
            throw new NullPointerException();
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.isolationLevel = isolationLevel;
        config.writeSkewAllowed = isolationLevel.doesAllowWriteSkew();
        config.inconsistentReadAllowed = isolationLevel.doesAllowInconsistentRead();
        config.unrepeatableReadAllowed = isolationLevel.doesAllowUnrepeatableRead();
        return config;
    }

    public GammaTransactionConfiguration setWriteLockMode(LockMode writeLockMode) {
        if (writeLockMode == null) {
            throw new NullPointerException();
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.writeLockMode = writeLockMode;
        config.writeLockModeAsInt = writeLockMode.asInt();
        return config;
    }

    public GammaTransactionConfiguration setReadLockMode(LockMode readLockMode) {
        if (readLockMode == null) {
            throw new NullPointerException();
        }

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.readLockMode = readLockMode;
        config.readLockModeAsInt = readLockMode.asInt();
        if (config.readLockModeAsInt > config.writeLockModeAsInt) {
            config.writeLockMode = config.readLockMode;
            config.writeLockModeAsInt = config.readLockModeAsInt;
        }
        return config;
    }


    public GammaTransactionConfiguration setFat() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.isFat = true;
        return config;
    }

    public GammaTransactionConfiguration addPermanentListener(TransactionListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        //we need to clone the list since the GammaTransactionConfiguration is considered to be immutable
        ArrayList<TransactionListener> newPermanentListeners = new ArrayList<TransactionListener>();
        if (permanentListeners != null) {
            newPermanentListeners.addAll(permanentListeners);
        }
        newPermanentListeners.add(listener);

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(this);
        config.permanentListeners = newPermanentListeners;
        return config;
    }

    @Override
    public String toString() {
        return "GammaTransactionConfiguration{" +
                "speculativeConfiguration=" + speculativeConfiguration +
                ", globalConflictCounter=" + globalConflictCounter +
                ", propagationLevel=" + propagationLevel +
                ", isolationLevel=" + isolationLevel +
                ", writeSkewAllowed=" + writeSkewAllowed +
                ", inconsistentReadAllowed=" + inconsistentReadAllowed +
                ", readLockMode=" + readLockMode +
                ", writeLockMode=" + writeLockMode +
                ", readLockModeAsInt=" + readLockModeAsInt +
                ", writeLockModeAsInt=" + writeLockModeAsInt +
                ", familyName='" + familyName + '\'' +
                ", isAnonymous=" + isAnonymous +
                ", interruptible=" + interruptible +
                ", readonly=" + readonly +
                ", spinCount=" + spinCount +
                ", dirtyCheck=" + dirtyCheck +
                ", minimalArrayTreeSize=" + minimalArrayTreeSize +
                ", trackReads=" + trackReads +
                ", blockingAllowed=" + blockingAllowed +
                ", maxRetries=" + maxRetries +
                ", speculativeConfigEnabled=" + speculative +
                ", maxFixedLengthTransactionSize=" + maxFixedLengthTransactionSize +
                ", backoffPolicy=" + backoffPolicy +
                ", timeoutNs=" + timeoutNs +
                ", traceLevel=" + traceLevel +
                ", controlFlowErrorsReused=" + controlFlowErrorsReused +
                ", isFat=" + isFat +
                ", maximumPoorMansConflictScanLength=" + maximumPoorMansConflictScanLength +
                ", permanentListeners=" + permanentListeners +
                '}';
    }

}
