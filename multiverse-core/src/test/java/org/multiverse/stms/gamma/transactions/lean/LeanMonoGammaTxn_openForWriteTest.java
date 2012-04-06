package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_openForWriteTest extends LeanGammaTxn_openForWriteTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }

    @Override
    public int getMaximumLength() {
        return 1;
    }
}
