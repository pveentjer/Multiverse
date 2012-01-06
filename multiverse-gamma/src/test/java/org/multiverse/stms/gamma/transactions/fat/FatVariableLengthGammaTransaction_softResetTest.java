package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTransaction_softResetTest extends FatGammaTransaction_softResetTest<FatVariableLengthGammaTransaction> {
    @Override
    public FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }
}
