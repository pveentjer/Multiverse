package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTransaction_initTest extends FatGammaTransaction_initTest<FatFixedLengthGammaTransaction> {
    @Override
    protected FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }
}
