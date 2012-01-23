package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_setAbortOnlyTest extends LeanGammaTransaction_setAbortOnlyTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }
}
