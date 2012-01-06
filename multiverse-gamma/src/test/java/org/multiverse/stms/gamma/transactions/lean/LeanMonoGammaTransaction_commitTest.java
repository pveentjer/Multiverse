package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTransaction_commitTest extends LeanGammaTransaction_commitTest<LeanMonoGammaTransaction> {

    @Override
    public LeanMonoGammaTransaction newTransaction() {
        return new LeanMonoGammaTransaction(stm);
    }

    @Override
    public void assertClearedAfterCommit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void assertClearedAfterAbort() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getMaximumLength() {
        return 1;
    }
}
