package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTxn_abortTest extends LeanGammaTxn_abortTest<LeanFixedLengthGammaTxn> {
    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }
}
