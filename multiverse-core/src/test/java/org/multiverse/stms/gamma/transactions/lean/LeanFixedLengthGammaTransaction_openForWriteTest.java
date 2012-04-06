package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class LeanFixedLengthGammaTransaction_openForWriteTest extends LeanGammaTransaction_openForWriteTest<LeanFixedLengthGammaTransaction> {

    @Override
    public LeanFixedLengthGammaTransaction newTransaction() {
        return new LeanFixedLengthGammaTransaction(stm);
    }


    @Override
    public int getMaximumLength() {
        return new GammaTxnConfiguration(stm).maxFixedLengthTransactionSize;
    }
}
