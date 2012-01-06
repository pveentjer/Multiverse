package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTransaction_locateTest extends FatGammaTransaction_locateTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }
}
