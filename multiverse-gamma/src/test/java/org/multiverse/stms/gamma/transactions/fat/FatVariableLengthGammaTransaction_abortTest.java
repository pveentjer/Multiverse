package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public class FatVariableLengthGammaTransaction_abortTest extends FatGammaTransaction_abortTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }

    @Override
    protected FatVariableLengthGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatVariableLengthGammaTransaction(config);
    }

    @Override
    protected void assertCleaned(FatVariableLengthGammaTransaction tx) {
        //throw new TodoException();
        //todo
    }
}
