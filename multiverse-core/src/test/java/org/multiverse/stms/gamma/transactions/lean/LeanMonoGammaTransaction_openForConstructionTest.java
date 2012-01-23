package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_openForConstructionTest
        extends LeanGammaTransaction_openForConstructionTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }
}
