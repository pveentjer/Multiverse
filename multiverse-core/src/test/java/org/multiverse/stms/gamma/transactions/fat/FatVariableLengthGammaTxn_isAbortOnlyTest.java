package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTxn_isAbortOnlyTest extends FatGammaTxn_isAbortOnlyTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }


}
