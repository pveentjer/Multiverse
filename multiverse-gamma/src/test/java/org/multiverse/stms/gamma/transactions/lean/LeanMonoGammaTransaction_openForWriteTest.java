package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_openForWriteTest extends LeanGammaTransaction_openForWriteTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }

    @Override
    public int getMaximumLength() {
        return 1;
    }
}
