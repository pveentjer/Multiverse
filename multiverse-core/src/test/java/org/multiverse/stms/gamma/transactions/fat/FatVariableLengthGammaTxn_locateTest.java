package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTxn_locateTest extends FatGammaTxn_locateTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }
}
