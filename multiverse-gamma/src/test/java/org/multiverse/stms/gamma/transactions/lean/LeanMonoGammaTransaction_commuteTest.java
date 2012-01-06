package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_commuteTest
        extends LeanGammaTransaction_commuteTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }
}
