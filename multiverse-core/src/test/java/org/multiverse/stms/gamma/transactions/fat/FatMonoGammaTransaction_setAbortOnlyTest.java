package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public class FatMonoGammaTransaction_setAbortOnlyTest extends FatGammaTransaction_setAbortOnlyTest<FatMonoGammaTransaction> {
    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }

    @Override
    protected FatMonoGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }
}
