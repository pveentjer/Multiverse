package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTransaction_locateTest extends FatGammaTransaction_locateTest<FatFixedLengthGammaTransaction> {

    @Override
    protected FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }
}
