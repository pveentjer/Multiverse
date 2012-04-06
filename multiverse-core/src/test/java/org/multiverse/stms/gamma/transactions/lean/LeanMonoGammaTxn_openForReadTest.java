package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_openForReadTest
        extends LeanGammaTxn_openForReadTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }

    @Override
    public int getMaximumLength() {
        return 1;
    }
}
