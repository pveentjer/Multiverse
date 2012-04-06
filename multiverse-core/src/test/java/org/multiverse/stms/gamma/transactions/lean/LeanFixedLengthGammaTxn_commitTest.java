package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

public class LeanFixedLengthGammaTxn_commitTest extends LeanGammaTxn_commitTest<LeanFixedLengthGammaTxn> {

    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
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
        return new GammaTxnConfig(stm).maxFixedLengthTransactionSize;
    }
}
