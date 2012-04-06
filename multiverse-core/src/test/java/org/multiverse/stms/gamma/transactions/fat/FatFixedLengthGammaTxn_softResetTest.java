package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTxn_softResetTest extends FatGammaTxn_softResetTest<FatFixedLengthGammaTxn> {
    @Override
    public FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }
}
