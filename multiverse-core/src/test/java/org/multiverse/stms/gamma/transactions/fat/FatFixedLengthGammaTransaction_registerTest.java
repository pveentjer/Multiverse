package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTransaction_registerTest extends FatGammaTransaction_registerTest<FatFixedLengthGammaTransaction> {

    @Override
    protected FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }
}
