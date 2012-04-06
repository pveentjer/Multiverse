package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.collections.TransactionalCollectionsFactory;
import org.multiverse.api.lifecycle.TransactionListener;
import org.multiverse.collections.NaiveTransactionalCollectionFactory;
import org.multiverse.stms.gamma.transactionalobjects.*;
import org.multiverse.stms.gamma.transactions.*;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransaction;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;


@SuppressWarnings({"ClassWithTooManyFields"})
public final class GammaStm implements Stm {

    /**
     * Creates a GammaStm implementation optimized for speed. This method probably will be invoked
     * by the {@link GlobalStmInstance}.
     *
     * @return the created GammaStm.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static GammaStm createFast() {
        return new GammaStm();
    }

    public final int defaultMaxRetries;
    public final int spinCount;
    public final BackoffPolicy defaultBackoffPolicy;
    public final GlobalConflictCounter globalConflictCounter = new GlobalConflictCounter();
    public final GammaRefFactoryImpl defaultRefFactory = new GammaRefFactoryImpl();
    public final GammaRefFactoryBuilder refFactoryBuilder = new GammaRefFactoryBuilderImpl();
    public final GammaTransactionExecutor defaultTransactionExecutor;
    public final GammaTxnConfiguration defaultConfig;
    public final NaiveTransactionalCollectionFactory defaultTransactionalCollectionFactory
            = new NaiveTransactionalCollectionFactory(this);
    public final int readBiasedThreshold;
    public final GammaOrElseBlock defaultOrElseBlock = new GammaOrElseBlock();

    public GammaStm() {
        this(new GammaStmConfiguration());
    }

    public GammaStm(GammaStmConfiguration configuration) {
        configuration.validate();

        this.defaultMaxRetries = configuration.maxRetries;
        this.spinCount = configuration.spinCount;
        this.defaultBackoffPolicy = configuration.backoffPolicy;
        this.defaultConfig = new GammaTxnConfiguration(this, configuration)
                .setSpinCount(spinCount);
        this.defaultTransactionExecutor = newTransactionFactoryBuilder()
                .setSpeculative(false)
                .newTransactionExecutor();
        this.readBiasedThreshold = configuration.readBiasedThreshold;
    }

    @Override
    public final GammaTransaction newDefaultTransaction() {
        return new FatVariableLengthGammaTransaction(this);
    }

    @Override
    public final GammaTransactionExecutor getDefaultTransactionExecutor() {
        return defaultTransactionExecutor;
    }

    @Override
    public final GammaOrElseBlock newOrElseBlock() {
        return defaultOrElseBlock;
    }

    public final GlobalConflictCounter getGlobalConflictCounter() {
        return globalConflictCounter;
    }

    private final class GammaTxnFactoryBuilderImpl implements GammaTxnFactoryBuilder {

        private final GammaTxnConfiguration config;

        GammaTxnFactoryBuilderImpl(final GammaTxnConfiguration config) {
            this.config = config;
        }

        @Override
        public final GammaTxnFactoryBuilder setFat() {
            if (config.isFat) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setFat());
        }

        @Override
        public final GammaTxnConfiguration getConfiguration() {
            return config;
        }

        @Override
        public GammaTxnFactoryBuilder addPermanentListener(final TransactionListener listener) {
            return new GammaTxnFactoryBuilderImpl(config.addPermanentListener(listener));
        }

        @Override
        public final GammaTxnFactoryBuilder setControlFlowErrorsReused(final boolean reused) {
            if (config.controlFlowErrorsReused = reused) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setControlFlowErrorsReused(reused));
        }

        @Override
        public final GammaTxnFactoryBuilder setReadLockMode(final LockMode lockMode) {
            if (config.readLockMode == lockMode) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setReadLockMode(lockMode));
        }

        @Override
        public final GammaTxnFactoryBuilder setWriteLockMode(final LockMode lockMode) {
            if (config.writeLockMode == lockMode) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setWriteLockMode(lockMode));
        }

        @Override
        public final GammaTxnFactoryBuilder setFamilyName(final String familyName) {
            if (config.familyName.equals(familyName)) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setFamilyName(familyName));
        }

        @Override
        public final GammaTxnFactoryBuilder setPropagationLevel(final PropagationLevel level) {
            if (level == config.propagationLevel) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setPropagationLevel(level));
        }

        @Override
        public final GammaTxnFactoryBuilder setBlockingAllowed(final boolean blockingAllowed) {
            if (blockingAllowed == config.blockingAllowed) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setBlockingAllowed(blockingAllowed));
        }

        @Override
        public final GammaTxnFactoryBuilder setIsolationLevel(final IsolationLevel isolationLevel) {
            if (isolationLevel == config.isolationLevel) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setIsolationLevel(isolationLevel));
        }

        @Override
        public final GammaTxnFactoryBuilder setTraceLevel(final TraceLevel traceLevel) {
            if (traceLevel == config.traceLevel) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setTraceLevel(traceLevel));
        }

        @Override
        public final GammaTxnFactoryBuilder setTimeoutNs(final long timeoutNs) {
            if (timeoutNs == config.timeoutNs) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setTimeoutNs(timeoutNs));
        }

        @Override
        public final GammaTxnFactoryBuilder setInterruptible(final boolean interruptible) {
            if (interruptible == config.interruptible) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setInterruptible(interruptible));
        }

        @Override
        public final GammaTxnFactoryBuilder setBackoffPolicy(final BackoffPolicy backoffPolicy) {
            //noinspection ObjectEquality
            if (backoffPolicy == config.backoffPolicy) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setBackoffPolicy(backoffPolicy));
        }

        @Override
        public final GammaTxnFactoryBuilder setDirtyCheckEnabled(final boolean dirtyCheckEnabled) {
            if (dirtyCheckEnabled == config.dirtyCheck) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setDirtyCheckEnabled(dirtyCheckEnabled));
        }

        @Override
        public final GammaTxnFactoryBuilder setSpinCount(final int spinCount) {
            if (spinCount == config.spinCount) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setSpinCount(spinCount));
        }

        @Override
        public final GammaTxnFactoryBuilder setSpeculative(final boolean enabled) {
            if (enabled == config.speculative) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(
                    config.setSpeculative(enabled));
        }

        @Override
        public final GammaTxnFactoryBuilder setReadonly(final boolean readonly) {
            if (readonly == config.readonly) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setReadonly(readonly));
        }

        @Override
        public final GammaTxnFactoryBuilder setReadTrackingEnabled(final boolean enabled) {
            if (enabled == config.trackReads) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setReadTrackingEnabled(enabled));
        }

        @Override
        public final GammaTxnFactoryBuilder setMaxRetries(final int maxRetries) {
            if (maxRetries == config.maxRetries) {
                return this;
            }

            return new GammaTxnFactoryBuilderImpl(config.setMaxRetries(maxRetries));
        }

        @Override
        public final GammaTransactionExecutor newTransactionExecutor() {
            config.init();

            if (isLean()) {
                return new LeanGammaTransactionExecutor(newTransactionFactory());
            } else {
                return new FatGammaTransactionExecutor(newTransactionFactory());
            }
        }

        private boolean isLean() {
            return config.propagationLevel == PropagationLevel.Requires;
        }

        @Override
        public GammaTxnFactory newTransactionFactory() {
            config.init();

            if (config.isSpeculative()) {
                return new SpeculativeGammaTxnFactory(config, this);
            } else {
                return new NonSpeculativeGammaTxnFactory(config,this);
            }
        }
    }

    @Override
    public final GammaRefFactory getDefaultRefFactory() {
        return defaultRefFactory;
    }

    private final class GammaRefFactoryImpl implements GammaRefFactory {
        @Override
        public final <E> GammaRef<E> newRef(E value) {
            return new GammaRef<E>(GammaStm.this, value);
        }

        @Override
        public final GammaIntRef newIntRef(int value) {
            return new GammaIntRef(GammaStm.this, value);
        }

        @Override
        public final GammaBooleanRef newBooleanRef(boolean value) {
            return new GammaBooleanRef(GammaStm.this, value);
        }

        @Override
        public final GammaDoubleRef newDoubleRef(double value) {
            return new GammaDoubleRef(GammaStm.this, value);
        }

        @Override
        public final GammaLongRef newLongRef(long value) {
            return new GammaLongRef(GammaStm.this, value);
        }
    }

    @Override
    public final GammaTxnFactoryBuilder newTransactionFactoryBuilder() {
        final GammaTxnConfiguration config = new GammaTxnConfiguration(this);
        return new GammaTxnFactoryBuilderImpl(config);
    }

    @Override
    public final TransactionalCollectionsFactory getDefaultTransactionalCollectionFactory() {
        return defaultTransactionalCollectionFactory;
    }

    @Override
    public final GammaRefFactoryBuilder getRefFactoryBuilder() {
        return refFactoryBuilder;
    }

    private final class GammaRefFactoryBuilderImpl implements GammaRefFactoryBuilder {
        @Override
        public GammaRefFactory build() {
            return new GammaRefFactoryImpl();
        }
    }

    private static final class NonSpeculativeGammaTxnFactory implements GammaTxnFactory {

        private final GammaTxnConfiguration config;
        private final GammaTxnFactoryBuilder builder;

        NonSpeculativeGammaTxnFactory(final GammaTxnConfiguration config, GammaTxnFactoryBuilder builder) {
            this.config = config.init();
            this.builder = builder;
        }

        @Override
        public TxnFactoryBuilder getTransactionFactoryBuilder() {
            return builder;
        }

        @Override
        public final GammaTxnConfiguration getConfiguration() {
            return config;
        }

        @Override
        public final GammaTransaction newTransaction() {
            return newTransaction(getThreadLocalGammaTransactionPool());
        }

        @Override
        public final GammaTransaction newTransaction(final GammaTransactionPool pool) {
            FatVariableLengthGammaTransaction tx = pool.takeMap();

            if (tx == null) {
                tx = new FatVariableLengthGammaTransaction(config);
            } else {
                tx.init(config);
            }

            return tx;
        }

        @Override
        public final GammaTransaction upgradeAfterSpeculativeFailure(final GammaTransaction tailingTx, final GammaTransactionPool pool) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class SpeculativeGammaTxnFactory implements GammaTxnFactory {

        private final GammaTxnConfiguration config;
        private final GammaTxnFactoryBuilder builder;

        SpeculativeGammaTxnFactory(final GammaTxnConfiguration config, GammaTxnFactoryBuilder builder) {
            this.config = config.init();
            this.builder = builder;
        }

        @Override
        public GammaTxnFactoryBuilder getTransactionFactoryBuilder() {
            return builder;
        }

        @Override
        public final GammaTxnConfiguration getConfiguration() {
            return config;
        }

        @Override
        public final GammaTransaction newTransaction() {
            return newTransaction(getThreadLocalGammaTransactionPool());
        }

        @Override
        public final GammaTransaction upgradeAfterSpeculativeFailure(final GammaTransaction failingTx, final GammaTransactionPool pool) {
            final GammaTransaction tx = newTransaction(pool);
            tx.copyForSpeculativeFailure(failingTx);
            return tx;
        }

        @Override
        public final GammaTransaction newTransaction(final GammaTransactionPool pool) {
            final SpeculativeGammaConfiguration speculativeConfiguration = config.speculativeConfiguration.get();
            final int length = speculativeConfiguration.minimalLength;

            if (length <= 1) {
                if (speculativeConfiguration.fat) {
                    FatMonoGammaTransaction tx = pool.takeFatMono();
                    if (tx == null) {
                        return new FatMonoGammaTransaction(config);
                    }

                    tx.init(config);
                    return tx;
                } else {
                    LeanMonoGammaTransaction tx = pool.takeLeanMono();
                    if (tx == null) {
                        return new LeanMonoGammaTransaction(config);
                    }

                    tx.init(config);
                    return tx;
                }

            } else if (length <= config.maxFixedLengthTransactionSize) {
                if (speculativeConfiguration.fat) {
                    final FatFixedLengthGammaTransaction tx = pool.takeFatFixedLength();
                    if (tx == null) {
                        return new FatFixedLengthGammaTransaction(config);
                    }

                    tx.init(config);
                    return tx;
                } else {
                    final LeanFixedLengthGammaTransaction tx = pool.takeLeanFixedLength();
                    if (tx == null) {
                        return new LeanFixedLengthGammaTransaction(config);
                    }

                    tx.init(config);
                    return tx;
                }

            } else {
                final FatVariableLengthGammaTransaction tx = pool.takeMap();
                if (tx == null) {
                    return new FatVariableLengthGammaTransaction(config);
                }

                tx.init(config);
                return tx;
            }
        }
    }
}
