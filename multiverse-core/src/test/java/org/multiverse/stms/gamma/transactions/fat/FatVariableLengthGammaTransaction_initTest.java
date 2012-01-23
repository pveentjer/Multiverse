package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTransaction_initTest extends FatGammaTransaction_initTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }
}
