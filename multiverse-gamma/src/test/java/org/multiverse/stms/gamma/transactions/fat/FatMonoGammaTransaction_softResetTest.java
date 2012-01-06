package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTransaction_softResetTest extends FatGammaTransaction_softResetTest<FatMonoGammaTransaction> {

    @Override
    public FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }
}
