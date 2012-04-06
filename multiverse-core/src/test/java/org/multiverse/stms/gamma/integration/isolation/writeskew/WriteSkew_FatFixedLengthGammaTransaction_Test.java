package org.multiverse.stms.gamma.integration.isolation.writeskew;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransaction;

public class WriteSkew_FatFixedLengthGammaTransaction_Test extends WriteSkew_AbstractTest<FatFixedLengthGammaTransaction> {

    @Override
    public FatFixedLengthGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatFixedLengthGammaTransaction(config);
    }
}
