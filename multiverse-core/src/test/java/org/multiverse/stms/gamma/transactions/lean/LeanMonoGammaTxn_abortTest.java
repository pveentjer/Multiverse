package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_abortTest extends LeanGammaTxn_abortTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }
}
