package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatVariableLengthGammaTxn_commuteTest extends FatGammaTxn_commuteTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }

    @Override
    protected FatVariableLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTxn(config);
    }

    @Override
    protected int getMaxCapacity() {
        return Integer.MAX_VALUE;
    }
}
