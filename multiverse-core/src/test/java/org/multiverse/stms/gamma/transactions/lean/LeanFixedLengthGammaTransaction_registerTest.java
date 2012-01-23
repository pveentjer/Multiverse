package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTransaction_registerTest
        extends LeanGammaTransaction_registerTest<LeanFixedLengthGammaTransaction> {

    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }
}
