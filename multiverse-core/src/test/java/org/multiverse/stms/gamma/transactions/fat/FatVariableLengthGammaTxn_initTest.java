package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTxn_initTest extends FatGammaTxn_initTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }
}
