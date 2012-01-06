package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTransaction_registerTest extends FatGammaTransaction_registerTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }
}
