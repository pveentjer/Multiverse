package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public class LeanMonoGammaTransaction_retryTest extends LeanGammaTransaction_retryTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }

    @Override
    public LeanMonoGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new LeanMonoGammaTransaction(config);
    }
}
