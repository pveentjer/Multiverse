package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransaction;

public class FatFixedLengthGammaTransaction_hardResetTest extends FatGammaTransaction_hardResetTest<GammaTransaction> {

    @Override
    protected GammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }
}
