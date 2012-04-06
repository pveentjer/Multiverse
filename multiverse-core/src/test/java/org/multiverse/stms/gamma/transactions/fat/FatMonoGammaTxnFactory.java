package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTxnPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTxnPool.getThreadLocalGammaTxnPool;

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
    public GammaTxn upgradeAfterSpeculativeFailure(GammaTxn failingTransaction, GammaTxnPool pool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GammaTxn newTransaction() {
        return newTransaction(getThreadLocalGammaTxnPool());
    }

    @Override
    public FatMonoGammaTxn newTransaction(GammaTxnPool pool) {
        FatMonoGammaTxn tx = pool.takeFatMono();
        if (tx == null) {
            tx = new FatMonoGammaTxn(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}
