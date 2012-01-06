package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public class FatMonoGammaTransaction_openForReadTest extends FatGammaTransaction_openForReadTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }

    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(new GammaTransactionConfiguration(stm));
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }
}
