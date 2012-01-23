package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTransaction_softResetTest extends FatGammaTransaction_softResetTest<FatFixedLengthGammaTransaction> {
    @Override
    public FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }
}
