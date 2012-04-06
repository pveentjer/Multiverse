package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTxn_setAbortOnlyTest extends LeanGammaTxn_setAbortOnlyTest<LeanFixedLengthGammaTxn> {

    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }
}
