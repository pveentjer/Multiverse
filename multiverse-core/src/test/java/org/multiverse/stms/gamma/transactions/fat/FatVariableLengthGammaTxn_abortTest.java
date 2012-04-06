package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatVariableLengthGammaTxn_abortTest extends FatGammaTxn_abortTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }

    @Override
    protected FatVariableLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTxn(config);
    }

    @Override
    protected void assertCleaned(FatVariableLengthGammaTxn tx) {
        //throw new TodoException();
        //todo
    }
}
