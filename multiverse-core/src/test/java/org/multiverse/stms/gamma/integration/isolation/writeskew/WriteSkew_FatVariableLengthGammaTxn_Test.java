package org.multiverse.stms.gamma.integration.isolation.writeskew;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;

public class WriteSkew_FatVariableLengthGammaTxn_Test extends WriteSkew_AbstractTest<FatVariableLengthGammaTxn> {

    @Override
    public FatVariableLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTxn(config);
    }
}
