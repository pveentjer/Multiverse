package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatVariableLengthGammaTransaction_setAbortOnlyTest extends FatGammaTransaction_setAbortOnlyTest<FatVariableLengthGammaTransaction> {
    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }

    @Override
    protected FatVariableLengthGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTransaction(config);
    }
}
