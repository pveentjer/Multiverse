package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTransaction_isAbortOnlyTest extends FatGammaTransaction_isAbortOnlyTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }


}
