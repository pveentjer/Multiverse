package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TransactionFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public final class FatVariableLengthGammaTransactionFactory implements GammaTransactionFactory {
    private final GammaTransactionConfiguration config;

    public FatVariableLengthGammaTransactionFactory(GammaStm stm) {
        this(new GammaTransactionConfiguration(stm));
    }

    public FatVariableLengthGammaTransactionFactory(GammaTransactionConfiguration config) {
        this.config = config.init();
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
