package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public class LeanFixedLengthGammaTransaction_commitTest extends LeanGammaTransaction_commitTest<LeanFixedLengthGammaTransaction> {

    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
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
        return new GammaTransactionConfiguration(stm).maxFixedLengthTransactionSize;
    }
}
