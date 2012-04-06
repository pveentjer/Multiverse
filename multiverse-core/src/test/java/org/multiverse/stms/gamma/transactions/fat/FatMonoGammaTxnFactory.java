package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public class FatMonoGammaTxnFactory implements GammaTxnFactory {

    private final GammaTxnConfiguration config;

    public FatMonoGammaTxnFactory(GammaStm stm) {
        this(new GammaTxnConfiguration(stm).setControlFlowErrorsReused(false));
    }

    public FatMonoGammaTxnFactory(GammaTxnConfiguration config) {
        this.config = config.setControlFlowErrorsReused(false).init();
    }

    @Override
    public TxnFactoryBuilder getTransactionFactoryBuilder() {
        throw new UnsupportedOperationException();
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
