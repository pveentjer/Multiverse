package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

public class LeanFixedLengthGammaTxn_openForWriteTest extends LeanGammaTxn_openForWriteTest<LeanFixedLengthGammaTxn> {

    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }


    @Override
    public int getMaximumLength() {
        return new GammaTxnConfig(stm).maxFixedLengthTransactionSize;
    }
}
