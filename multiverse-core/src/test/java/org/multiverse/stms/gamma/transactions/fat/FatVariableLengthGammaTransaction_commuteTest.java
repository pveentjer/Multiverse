package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public class FatVariableLengthGammaTransaction_commuteTest extends FatGammaTransaction_commuteTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }

    @Override
    protected FatVariableLengthGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatVariableLengthGammaTransaction(config);
    }

    @Override
    protected int getMaxCapacity() {
        return Integer.MAX_VALUE;
    }
}
