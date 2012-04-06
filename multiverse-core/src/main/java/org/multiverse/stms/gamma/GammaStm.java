package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.collections.TxnCollectionsFactory;
import org.multiverse.api.lifecycle.TxnListener;
import org.multiverse.collections.NaiveTxnCollectionFactory;
import org.multiverse.stms.gamma.transactionalobjects.*;
import org.multiverse.stms.gamma.transactions.*;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTxnPool.getThreadLocalGammaTxnPool;


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
    public final GammaTxnRefFactoryImpl defaultRefFactory = new GammaTxnRefFactoryImpl();
    public final GammaTxnRefFactoryBuilder refFactoryBuilder = new GammaTxnRefFactoryBuilderImpl();
    public final GammaTxnExecutor defaultxnExecutor;
    public final GammaTxnConfig defaultConfig;
    public final NaiveTxnCollectionFactory defaultTransactionalCollectionFactory
            = new NaiveTxnCollectionFactory(this);
    public final int readBiasedThreshold;
    public final GammaOrElseBlock defaultOrElseBlock = new GammaOrElseBlock();

    public GammaStm() {
        this(new GammaStmConfig());
    }

    public GammaStm(GammaStmConfig config) {
        config.validate();

        this.defaultMaxRetries = config.maxRetries;
        this.spinCount = config.spinCount;
        this.defaultBackoffPolicy = config.backoffPolicy;
        this.defaultConfig = new GammaTxnConfig(this, config)
                .setSpinCount(spinCount);
        this.defaultxnExecutor = newTxnFactoryBuilder()
                .setSpeculative(false)
                .newTxnExecutor();
        this.readBiasedThreshold = config.readBiasedThreshold;
    }

    @Override
    public final GammaTxn newDefaultTxn() {
        return new FatVariableLengthGammaTxn(this);
    }

    @Override
    public final GammaTxnExecutor getDefaultTxnExecutor() {
        return defaultxnExecutor;
    }

    @Override
    public final GammaOrElseBlock newOrElseBlock() {
        return defaultOrElseBlock;
    }

    public final GlobalConflictCounter getGlobalConflictCounter() {
        return globalConflictCounter;
    }

    private final class GammaTxnFactoryBuilderImpl implements GammaTxnFactoryBuilder {

        private final GammaTxnConfig config;

        GammaTxnFactoryBuilderImpl(final GammaTxnConfig config) {
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
        public final GammaTxnConfig getConfig() {
            return config;
        }

        @Override
        public GammaTxnFactoryBuilder addPermanentListener(final TxnListener listener) {
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
        public final GammaTxnExecutor newTxnExecutor() {
            config.init();

            if (isLean()) {
                return new LeanGammaTxnExecutor(newTransactionFactory());
            } else {
                return new FatGammaTxnExecutor(newTransactionFactory());
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
    public final GammaTxnRefFactory getDefaultRefFactory() {
        return defaultRefFactory;
    }

    private final class GammaTxnRefFactoryImpl implements GammaTxnRefFactory {
        @Override
        public final <E> GammaTxnRef<E> newTxnRef(E value) {
            return new GammaTxnRef<E>(GammaStm.this, value);
        }

        @Override
        public final GammaTxnInteger newTxnInteger(int value) {
            return new GammaTxnInteger(GammaStm.this, value);
        }

        @Override
        public final GammaTxnBoolean newTxnBoolean(boolean value) {
            return new GammaTxnBoolean(GammaStm.this, value);
        }

        @Override
        public final GammaTxnDouble newTxnDouble(double value) {
            return new GammaTxnDouble(GammaStm.this, value);
        }

        @Override
        public final GammaTxnLong newTxnLong(long value) {
            return new GammaTxnLong(GammaStm.this, value);
        }
    }

    @Override
    public final GammaTxnFactoryBuilder newTxnFactoryBuilder() {
        final GammaTxnConfig config = new GammaTxnConfig(this);
        return new GammaTxnFactoryBuilderImpl(config);
    }

    @Override
    public final TxnCollectionsFactory getDefaultTxnCollectionFactory() {
        return defaultTransactionalCollectionFactory;
    }

    @Override
    public final GammaTxnRefFactoryBuilder getTxRefFactoryBuilder() {
        return refFactoryBuilder;
    }

    private final class GammaTxnRefFactoryBuilderImpl implements GammaTxnRefFactoryBuilder {
        @Override
        public GammaTxnRefFactory build() {
            return new GammaTxnRefFactoryImpl();
        }
    }

    private static final class NonSpeculativeGammaTxnFactory implements GammaTxnFactory {

        private final GammaTxnConfig config;
        private final GammaTxnFactoryBuilder builder;

        NonSpeculativeGammaTxnFactory(final GammaTxnConfig config, GammaTxnFactoryBuilder builder) {
            this.config = config.init();
            this.builder = builder;
        }

        @Override
        public TxnFactoryBuilder getTransactionFactoryBuilder() {
            return builder;
        }

        @Override
        public final GammaTxnConfig getConfig() {
            return config;
        }

        @Override
        public final GammaTxn newTransaction() {
            return newTransaction(getThreadLocalGammaTxnPool());
        }

        @Override
        public final GammaTxn newTransaction(final GammaTxnPool pool) {
            FatVariableLengthGammaTxn tx = pool.takeMap();

            if (tx == null) {
                tx = new FatVariableLengthGammaTxn(config);
            } else {
                tx.init(config);
            }

            return tx;
        }

        @Override
        public final GammaTxn upgradeAfterSpeculativeFailure(final GammaTxn tailingTx, final GammaTxnPool pool) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class SpeculativeGammaTxnFactory implements GammaTxnFactory {

        private final GammaTxnConfig config;
        private final GammaTxnFactoryBuilder builder;

        SpeculativeGammaTxnFactory(final GammaTxnConfig config, GammaTxnFactoryBuilder builder) {
            this.config = config.init();
            this.builder = builder;
        }

        @Override
        public GammaTxnFactoryBuilder getTransactionFactoryBuilder() {
            return builder;
        }

        @Override
        public final GammaTxnConfig getConfig() {
            return config;
        }

        @Override
        public final GammaTxn newTransaction() {
            return newTransaction(getThreadLocalGammaTxnPool());
        }

        @Override
        public final GammaTxn upgradeAfterSpeculativeFailure(final GammaTxn failingTx, final GammaTxnPool pool) {
            final GammaTxn tx = newTransaction(pool);
            tx.copyForSpeculativeFailure(failingTx);
            return tx;
        }

        @Override
        public final GammaTxn newTransaction(final GammaTxnPool pool) {
            final SpeculativeGammaConfiguration speculativeConfiguration = config.speculativeConfiguration.get();
            final int length = speculativeConfiguration.minimalLength;

            if (length <= 1) {
                if (speculativeConfiguration.fat) {
                    FatMonoGammaTxn tx = pool.takeFatMono();
                    if (tx == null) {
                        return new FatMonoGammaTxn(config);
                    }

                    tx.init(config);
                    return tx;
                } else {
                    LeanMonoGammaTxn tx = pool.takeLeanMono();
                    if (tx == null) {
                        return new LeanMonoGammaTxn(config);
                    }

                    tx.init(config);
                    return tx;
                }

            } else if (length <= config.maxFixedLengthTransactionSize) {
                if (speculativeConfiguration.fat) {
                    final FatFixedLengthGammaTxn tx = pool.takeFatFixedLength();
                    if (tx == null) {
                        return new FatFixedLengthGammaTxn(config);
                    }

                    tx.init(config);
                    return tx;
                } else {
                    final LeanFixedLengthGammaTxn tx = pool.takeLeanFixedLength();
                    if (tx == null) {
                        return new LeanFixedLengthGammaTxn(config);
                    }

                    tx.init(config);
                    return tx;
                }

            } else {
                final FatVariableLengthGammaTxn tx = pool.takeMap();
                if (tx == null) {
                    return new FatVariableLengthGammaTxn(config);
                }

                tx.init(config);
                return tx;
            }
        }
    }
}
