package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_setAbortOnlyTest extends LeanGammaTxn_setAbortOnlyTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }
}
