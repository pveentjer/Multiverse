package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTransaction_setAbortOnlyTest extends LeanGammaTransaction_setAbortOnlyTest<LeanFixedLengthGammaTransaction> {

    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }
}
