package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_commitTest extends LeanGammaTxn_commitTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
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
