package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTransaction_registerTest extends FatGammaTransaction_registerTest<FatMonoGammaTransaction> {
    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }
}
