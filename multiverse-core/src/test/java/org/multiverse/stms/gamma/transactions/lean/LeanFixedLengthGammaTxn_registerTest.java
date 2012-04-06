package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTxn_registerTest
        extends LeanGammaTxn_registerTest<LeanFixedLengthGammaTxn> {

    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }
}
