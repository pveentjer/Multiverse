package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTransaction_commuteTest extends LeanGammaTransaction_commuteTest<LeanFixedLengthGammaTransaction> {

    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }
}
