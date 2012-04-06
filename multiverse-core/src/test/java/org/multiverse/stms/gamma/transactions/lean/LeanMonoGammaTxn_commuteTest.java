package org.multiverse.stms.gamma.transactions.lean;

public class LeanMonoGammaTxn_commuteTest
        extends LeanGammaTxn_commuteTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }
}
