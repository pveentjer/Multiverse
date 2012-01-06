package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTransaction_openForConstructionTest
        extends LeanGammaTransaction_openForConstructionTest<LeanFixedLengthGammaTransaction> {

    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }
}
