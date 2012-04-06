package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTxn_softResetTest extends FatGammaTxn_softResetTest<FatMonoGammaTxn> {

    @Override
    public FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }
}
