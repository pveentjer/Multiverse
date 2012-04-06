package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTxn_prepareTest extends LeanGammaTxn_prepareTest<LeanFixedLengthGammaTxn> {
    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }
}
