package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_openForConstructionTest
        extends LeanGammaTxn_openForConstructionTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }
}
