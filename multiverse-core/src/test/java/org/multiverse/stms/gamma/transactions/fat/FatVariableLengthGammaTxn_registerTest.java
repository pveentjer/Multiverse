package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTxn_registerTest extends FatGammaTxn_registerTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }
}
