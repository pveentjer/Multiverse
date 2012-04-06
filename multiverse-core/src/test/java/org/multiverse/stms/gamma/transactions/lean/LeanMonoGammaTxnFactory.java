package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public class LeanMonoGammaTxnFactory implements GammaTxnFactory {

    private final GammaTxnConfiguration config;

    public LeanMonoGammaTxnFactory(GammaStm stm) {
        this(new GammaTxnConfiguration(stm).setControlFlowErrorsReused(false));
    }

    public LeanMonoGammaTxnFactory(GammaTxnConfiguration config) {
        this.config = config.setControlFlowErrorsReused(false).init();
    }

    @Override
    public GammaTxnConfiguration getConfiguration() {
        return config;
    }

    @Override
    public GammaTransaction upgradeAfterSpeculativeFailure(GammaTransaction failingTransaction, GammaTransactionPool pool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TxnFactoryBuilder getTransactionFactoryBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return newTransaction(getThreadLocalGammaTransactionPool());
    }

    @Override
    public LeanMonoGammaTransaction newTransaction(GammaTransactionPool pool) {
        LeanMonoGammaTransaction tx = pool.takeLeanMono();
        if (tx == null) {
            tx = new LeanMonoGammaTransaction(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}

