package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTransaction_isAbortOnlyTest extends FatGammaTransaction_isAbortOnlyTest<FatFixedLengthGammaTransaction> {

    @Override
    protected FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }
}
