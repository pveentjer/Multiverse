package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatVariableLengthGammaTxn_prepareTest extends FatGammaTxn_prepareTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }

    @Override
    protected FatVariableLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTxn(config);
    }
}
