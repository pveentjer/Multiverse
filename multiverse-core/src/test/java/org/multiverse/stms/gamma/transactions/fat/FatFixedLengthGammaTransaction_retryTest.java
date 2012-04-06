package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatFixedLengthGammaTransaction_retryTest extends FatGammaTransaction_retryTest<FatFixedLengthGammaTransaction> {

    @Override
    protected FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }

    @Override
    protected FatFixedLengthGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatFixedLengthGammaTransaction(config);
    }
}
