package org.multiverse.stms.gamma.integration.isolation.writeskew;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;

public class WriteSkew_FatFixedLengthGammaTxn_Test extends WriteSkew_AbstractTest<FatFixedLengthGammaTxn> {

    @Override
    public FatFixedLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatFixedLengthGammaTxn(config);
    }
}
