package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTxn_openForConstructionTest
        extends LeanGammaTxn_openForConstructionTest<LeanFixedLengthGammaTxn> {

    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }
}
