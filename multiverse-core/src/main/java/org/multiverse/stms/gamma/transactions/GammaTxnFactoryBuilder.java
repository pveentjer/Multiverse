package org.multiverse.stms.gamma.transactions;

import org.multiverse.api.*;
import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.api.lifecycle.TransactionListener;
import org.multiverse.stms.gamma.GammaTransactionExecutor;

/**
 * A {@link org.multiverse.api.TxnFactoryBuilder} tailored for the {@link org.multiverse.stms.gamma.GammaStm}.
 *
 * @author Peter Veentjer.
 */
public interface GammaTxnFactoryBuilder extends TxnFactoryBuilder {

    @Override
    GammaTxnConfiguration getConfiguration();

    GammaTxnFactoryBuilder setFat();

    @Override
    GammaTxnFactoryBuilder setControlFlowErrorsReused(boolean reused);

    @Override
    GammaTxnFactoryBuilder setReadLockMode(LockMode lockMode);

    @Override
    GammaTxnFactoryBuilder setWriteLockMode(LockMode lockMode);

    @Override
    GammaTxnFactoryBuilder setFamilyName(String familyName);

    @Override
    GammaTxnFactoryBuilder setPropagationLevel(PropagationLevel propagationLevel);

    @Override
    GammaTxnFactoryBuilder addPermanentListener(TransactionListener listener);

    @Override
    GammaTxnFactoryBuilder setTraceLevel(TraceLevel traceLevel);

    @Override
    GammaTxnFactoryBuilder setTimeoutNs(long timeoutNs);

    @Override
    GammaTxnFactoryBuilder setInterruptible(boolean interruptible);

    @Override
    GammaTxnFactoryBuilder setBackoffPolicy(BackoffPolicy backoffPolicy);

    @Override
    GammaTxnFactoryBuilder setDirtyCheckEnabled(boolean dirtyCheckEnabled);

    @Override
    GammaTxnFactoryBuilder setSpinCount(int spinCount);

    @Override
    GammaTxnFactoryBuilder setReadonly(boolean readonly);

    @Override
    GammaTxnFactoryBuilder setReadTrackingEnabled(boolean enabled);

    @Override
    GammaTxnFactoryBuilder setSpeculative(boolean enabled);

    @Override
    GammaTxnFactoryBuilder setMaxRetries(int maxRetries);

    @Override
    GammaTxnFactoryBuilder setIsolationLevel(IsolationLevel isolationLevel);

    @Override
    GammaTxnFactoryBuilder setBlockingAllowed(boolean blockingAllowed);

    @Override
    GammaTxnFactory newTransactionFactory();

    @Override
    GammaTransactionExecutor newTransactionExecutor();
}
