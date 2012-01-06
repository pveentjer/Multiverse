package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTransaction_isAbortOnlyTest extends FatGammaTransaction_isAbortOnlyTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(stm);
    }
}
