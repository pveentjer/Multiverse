package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTransaction_abortTest extends LeanGammaTransaction_abortTest<LeanFixedLengthGammaTransaction> {
    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }
}
