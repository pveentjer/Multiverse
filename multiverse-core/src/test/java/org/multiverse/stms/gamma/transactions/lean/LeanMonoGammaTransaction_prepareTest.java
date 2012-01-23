package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_prepareTest extends LeanGammaTransaction_prepareTest<LeanMonoGammaTransaction>{

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }
}
