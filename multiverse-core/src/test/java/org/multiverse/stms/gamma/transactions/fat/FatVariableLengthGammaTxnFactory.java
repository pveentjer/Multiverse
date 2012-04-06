package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTxnPool;

import static org.multiverse.stms.gamma.transactions.ThreadLocalGammaTxnPool.getThreadLocalGammaTxnPool;

public final class FatVariableLengthGammaTxnFactory implements GammaTxnFactory {
    private final GammaTxnConfig config;

    public FatVariableLengthGammaTxnFactory(GammaStm stm) {
        this(new GammaTxnConfig(stm));
    }

    public FatVariableLengthGammaTxnFactory(GammaTxnConfig config) {
        this.config = config.init();
    }

    @Override
    public TxnFactoryBuilder getTxnFactoryBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GammaTxnConfig getConfig() {
        return config;
    }

    @Override
    public FatVariableLengthGammaTxn upgradeAfterSpeculativeFailure(GammaTxn failingTransaction, GammaTxnPool pool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FatVariableLengthGammaTxn newTxn() {
        return newTransaction(getThreadLocalGammaTxnPool());
    }

    @Override
    public FatVariableLengthGammaTxn newTransaction(GammaTxnPool pool) {
        FatVariableLengthGammaTxn tx = pool.takeMap();
        if (tx == null) {
            tx = new FatVariableLengthGammaTxn(config);
        } else {
            tx.init(config);
        }
        return tx;
    }
}
