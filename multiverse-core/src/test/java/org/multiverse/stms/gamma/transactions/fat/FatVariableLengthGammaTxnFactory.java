package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public final class FatVariableLengthGammaTxnFactory implements GammaTxnFactory {
    private final GammaTxnConfiguration config;

    public FatVariableLengthGammaTxnFactory(GammaStm stm) {
        this(new GammaTxnConfiguration(stm));
    }

    public FatVariableLengthGammaTxnFactory(GammaTxnConfiguration config) {
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
    public FatVariableLengthGammaTransaction upgradeAfterSpeculativeFailure(GammaTransaction failingTransaction, GammaTransactionPool pool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FatVariableLengthGammaTransaction newTransaction() {
        return newTransaction(getThreadLocalGammaTransactionPool());
    }

    @Override
    public FatVariableLengthGammaTransaction newTransaction(GammaTransactionPool pool) {
        FatVariableLengthGammaTransaction tx = pool.takeMap();
        if (tx == null) {
            tx = new FatVariableLengthGammaTransaction(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}
