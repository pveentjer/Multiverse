package org.multiverse.stms.gamma.integration.isolation.writeskew;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransaction;

public class WriteSkew_FatVariableLengthGammaTransaction_Test extends WriteSkew_AbstractTest<FatVariableLengthGammaTransaction> {

    @Override
    public FatVariableLengthGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTransaction(config);
    }
}
