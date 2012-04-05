package org.multiverse.stms.gamma.transactions;

import org.multiverse.api.BackoffPolicy;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.api.PropagationLevel;
import org.multiverse.api.TraceLevel;
import org.multiverse.api.TransactionFactoryBuilder;
import org.multiverse.api.lifecycle.TransactionListener;
import org.multiverse.stms.gamma.GammaTransactionExecutor;

/**
 * A {@link TransactionFactoryBuilder} tailored for the {@link org.multiverse.stms.gamma.GammaStm}.
 *
 * @author Peter Veentjer.
 */
public interface GammaTransactionFactoryBuilder extends TransactionFactoryBuilder {

    @Override
    GammaTransactionConfiguration getConfiguration();

    GammaTransactionFactoryBuilder setFat();

    @Override
    GammaTransactionFactoryBuilder setControlFlowErrorsReused(boolean reused);

    @Override
    GammaTransactionFactoryBuilder setReadLockMode(LockMode lockMode);

    @Override
    GammaTransactionFactoryBuilder setWriteLockMode(LockMode lockMode);

    @Override
    GammaTransactionFactoryBuilder setFamilyName(String familyName);

    @Override
    GammaTransactionFactoryBuilder setPropagationLevel(PropagationLevel propagationLevel);

    @Override
    GammaTransactionFactoryBuilder addPermanentListener(TransactionListener listener);

    @Override
    GammaTransactionFactoryBuilder setTraceLevel(TraceLevel traceLevel);

    @Override
    GammaTransactionFactoryBuilder setTimeoutNs(long timeoutNs);

    @Override
    GammaTransactionFactoryBuilder setInterruptible(boolean interruptible);

    @Override
    GammaTransactionFactoryBuilder setBackoffPolicy(BackoffPolicy backoffPolicy);

    @Override
    GammaTransactionFactoryBuilder setDirtyCheckEnabled(boolean dirtyCheckEnabled);

    @Override
    GammaTransactionFactoryBuilder setSpinCount(int spinCount);

    @Override
    GammaTransactionFactoryBuilder setReadonly(boolean readonly);

    @Override
    GammaTransactionFactoryBuilder setReadTrackingEnabled(boolean enabled);

    @Override
    GammaTransactionFactoryBuilder setSpeculative(boolean enabled);

    @Override
    GammaTransactionFactoryBuilder setMaxRetries(int maxRetries);

    @Override
    GammaTransactionFactoryBuilder setIsolationLevel(IsolationLevel isolationLevel);

    @Override
    GammaTransactionFactoryBuilder setBlockingAllowed(boolean blockingAllowed);

    @Override
    GammaTransactionFactory newTransactionFactory();

    @Override
    GammaTransactionExecutor newTransactionExecutor();
}
