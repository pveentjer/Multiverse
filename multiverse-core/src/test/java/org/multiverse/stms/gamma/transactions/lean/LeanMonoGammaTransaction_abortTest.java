package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_abortTest extends LeanGammaTransaction_abortTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }
}
