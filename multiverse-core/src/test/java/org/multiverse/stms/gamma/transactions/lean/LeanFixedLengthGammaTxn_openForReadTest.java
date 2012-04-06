package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class LeanFixedLengthGammaTxn_openForReadTest extends LeanGammaTxn_openForReadTest<LeanFixedLengthGammaTxn> {

    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }

    @Override
    public int getMaximumLength() {
        return new GammaTxnConfiguration(stm).maxFixedLengthTransactionSize;
    }

}
