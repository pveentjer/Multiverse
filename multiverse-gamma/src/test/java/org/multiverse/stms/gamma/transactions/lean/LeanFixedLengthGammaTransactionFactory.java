package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.api.TransactionFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.GammaTransactionPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTransactionPool.getThreadLocalGammaTransactionPool;

public class LeanFixedLengthGammaTransactionFactory implements GammaTransactionFactory {

    private final GammaTransactionConfiguration config;

    public LeanFixedLengthGammaTransactionFactory(GammaStm stm) {
        this(new GammaTransactionConfiguration(stm));
    }

    public LeanFixedLengthGammaTransactionFactory(GammaStm stm, int fixedLengthSize) {
        this(new GammaTransactionConfiguration(stm, fixedLengthSize));
    }

    public LeanFixedLengthGammaTransactionFactory(GammaTransactionConfiguration config) {
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