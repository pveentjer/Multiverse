package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTransaction_locateTest extends FatGammaTransaction_locateTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }
}
