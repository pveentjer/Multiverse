package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTransaction_prepareTest extends LeanGammaTransaction_prepareTest<LeanFixedLengthGammaTransaction> {
    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }
}
