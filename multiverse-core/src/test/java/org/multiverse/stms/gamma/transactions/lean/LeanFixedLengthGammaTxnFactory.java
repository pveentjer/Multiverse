package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTxnPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTxnPool.getThreadLocalGammaTxnPool;

public class LeanFixedLengthGammaTxnFactory implements GammaTxnFactory {

    private final GammaTxnConfiguration config;

    public LeanFixedLengthGammaTxnFactory(GammaStm stm) {
        this(new GammaTxnConfiguration(stm));
    }

    public LeanFixedLengthGammaTxnFactory(GammaStm stm, int fixedLengthSize) {
        this(new GammaTxnConfiguration(stm, fixedLengthSize));
    }

    public LeanFixedLengthGammaTxnFactory(GammaTxnConfiguration config) {
        this.config = config.init();
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
    public LeanFixedLengthGammaTxn newTransaction() {
        return newTransaction(getThreadLocalGammaTxnPool());
    }

    @Override
    public LeanFixedLengthGammaTxn newTransaction(GammaTxnPool pool) {
        LeanFixedLengthGammaTxn tx = pool.takeLeanFixedLength();
        if (tx == null) {
            tx = new LeanFixedLengthGammaTxn(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}