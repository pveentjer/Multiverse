package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TransactionFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public class FatMonoGammaTransactionFactory implements GammaTransactionFactory {

    private final GammaTransactionConfiguration config;

    public FatMonoGammaTransactionFactory(GammaStm stm) {
        this(new GammaTransactionConfiguration(stm).setControlFlowErrorsReused(false));
    }

    public FatMonoGammaTransactionFactory(GammaTransactionConfiguration config) {
        this.config = config.setControlFlowErrorsReused(false).init();
    }

    @Override
    public TransactionFactoryBuilder getTransactionFactoryBuilder() {
        throw new UnsupportedOperationException();
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
    public GammaTransaction newTransaction() {
        return newTransaction(getThreadLocalGammaTransactionPool());
    }

    @Override
    public FatMonoGammaTransaction newTransaction(GammaTransactionPool pool) {
        FatMonoGammaTransaction tx = pool.takeFatMono();
        if (tx == null) {
            tx = new FatMonoGammaTransaction(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}
