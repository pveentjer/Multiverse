package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_registerTest extends LeanGammaTransaction_registerTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }
}
