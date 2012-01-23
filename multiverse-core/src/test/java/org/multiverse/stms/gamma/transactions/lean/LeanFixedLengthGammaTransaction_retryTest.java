package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;


public class LeanFixedLengthGammaTransaction_retryTest extends LeanGammaTransaction_retryTest<LeanFixedLengthGammaTransaction>{
    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }

    @Override
    public LeanFixedLengthGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new LeanFixedLengthGammaTransaction(config);
    }
}
