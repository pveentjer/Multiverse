package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTransaction_hardResetTest extends FatGammaTransaction_hardResetTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }
}
