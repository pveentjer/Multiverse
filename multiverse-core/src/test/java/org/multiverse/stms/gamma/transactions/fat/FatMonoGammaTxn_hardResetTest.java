package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTxn_hardResetTest extends FatGammaTxn_hardResetTest<FatMonoGammaTxn> {

    @Override
    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }
}
