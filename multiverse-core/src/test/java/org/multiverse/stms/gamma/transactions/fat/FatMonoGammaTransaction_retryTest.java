package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatMonoGammaTransaction_retryTest extends FatGammaTransaction_retryTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }

    @Override
    protected FatMonoGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }
}
