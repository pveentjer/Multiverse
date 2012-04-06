package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

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
    public GammaTransaction upgradeAfterSpeculativeFailure(GammaTransaction failingTransaction, GammaTransactionPool pool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return newTransaction(getThreadLocalGammaTransactionPool());
    }

    @Override
    public LeanFixedLengthGammaTransaction newTransaction(GammaTransactionPool pool) {
        LeanFixedLengthGammaTransaction tx = pool.takeLeanFixedLength();
        if (tx == null) {
            tx = new LeanFixedLengthGammaTransaction(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}