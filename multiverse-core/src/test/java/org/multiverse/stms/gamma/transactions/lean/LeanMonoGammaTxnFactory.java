package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTxnPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTxnPool.getThreadLocalGammaTxnPool;

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
    public GammaTxn upgradeAfterSpeculativeFailure(GammaTxn failingTransaction, GammaTxnPool pool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TxnFactoryBuilder getTransactionFactoryBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return newTransaction(getThreadLocalGammaTxnPool());
    }

    @Override
    public LeanMonoGammaTxn newTransaction(GammaTxnPool pool) {
        LeanMonoGammaTxn tx = pool.takeLeanMono();
        if (tx == null) {
            tx = new LeanMonoGammaTxn(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}

