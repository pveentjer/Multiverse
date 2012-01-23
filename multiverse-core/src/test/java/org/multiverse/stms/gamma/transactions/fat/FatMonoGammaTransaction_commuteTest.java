package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public class FatMonoGammaTransaction_commuteTest extends FatGammaTransaction_commuteTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }

    @Override
    protected FatMonoGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }
}
