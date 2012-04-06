package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_registerTest extends LeanGammaTxn_registerTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }
}
