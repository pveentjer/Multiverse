package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTxnPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTxnPool.getThreadLocalGammaTxnPool;

public class FatFixedLengthGammaTxnFactory implements GammaTxnFactory {

    private final GammaTxnConfiguration config;

    public FatFixedLengthGammaTxnFactory(GammaStm stm) {
        this(new GammaTxnConfiguration(stm));
    }

    public FatFixedLengthGammaTxnFactory(GammaTxnConfiguration config) {
        this.config = config.init();
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
    public FatFixedLengthGammaTxn newTransaction() {
        return newTransaction(getThreadLocalGammaTxnPool());
    }

    @Override
    public FatFixedLengthGammaTxn newTransaction(GammaTxnPool pool) {
        FatFixedLengthGammaTxn tx = pool.takeFatFixedLength();
        if (tx == null) {
            tx = new FatFixedLengthGammaTxn(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}