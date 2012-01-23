package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTransaction_initTest extends FatGammaTransaction_initTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }
}
