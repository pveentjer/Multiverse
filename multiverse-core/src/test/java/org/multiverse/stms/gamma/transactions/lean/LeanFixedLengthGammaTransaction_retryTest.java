package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;


public class LeanFixedLengthGammaTransaction_retryTest extends LeanGammaTransaction_retryTest<LeanFixedLengthGammaTransaction>{
    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }

    @Override
    public LeanFixedLengthGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new LeanFixedLengthGammaTransaction(config);
    }
}
