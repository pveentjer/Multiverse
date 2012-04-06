package org.multiverse.stms.gamma.transactions.fat;

public class FatVariableLengthGammaTxn_softResetTest extends FatGammaTxn_softResetTest<FatVariableLengthGammaTxn> {
    @Override
    public FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }
}
