package org.multiverse.stms.gamma.transactions.lean;

public class LeanFixedLengthGammaTxn_commuteTest extends LeanGammaTxn_commuteTest<LeanFixedLengthGammaTxn> {

    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }
}
