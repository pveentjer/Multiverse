package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TransactionFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public class FatFixedLengthGammaTransactionFactory implements GammaTransactionFactory {

    private final GammaTransactionConfiguration config;

    public FatFixedLengthGammaTransactionFactory(GammaStm stm) {
        this(new GammaTransactionConfiguration(stm));
    }

    public FatFixedLengthGammaTransactionFactory(GammaTransactionConfiguration config) {
        this.config = config.init();
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
    public FatFixedLengthGammaTransaction newTransaction() {
        return newTransaction(getThreadLocalGammaTransactionPool());
    }

    @Override
    public FatFixedLengthGammaTransaction newTransaction(GammaTransactionPool pool) {
        FatFixedLengthGammaTransaction tx = pool.takeFatFixedLength();
        if (tx == null) {
            tx = new FatFixedLengthGammaTransaction(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}