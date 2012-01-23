package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.api.TransactionFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public class LeanMonoGammaTransactionFactory implements GammaTransactionFactory {

    private final GammaTransactionConfiguration config;

    public LeanMonoGammaTransactionFactory(GammaStm stm) {
        this(new GammaTransactionConfiguration(stm).setControlFlowErrorsReused(false));
    }

    public LeanMonoGammaTransactionFactory(GammaTransactionConfiguration config) {
        this.config = config.setControlFlowErrorsReused(false).init();
    }

    @Override
    public GammaTransactionConfiguration getConfiguration() {
        return config;
    }

    @Override
    public GammaTransaction upgradeAfterSpeculativeFailure(GammaTransaction failingTransaction, GammaTransactionPool pool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionFactoryBuilder getTransactionFactoryBuilder() {
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

