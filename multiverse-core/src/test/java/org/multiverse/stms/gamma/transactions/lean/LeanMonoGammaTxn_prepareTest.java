package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_prepareTest extends LeanGammaTxn_prepareTest<LeanMonoGammaTxn>{

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }
}
