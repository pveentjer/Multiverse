package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTxn_locateTest extends FatGammaTxn_locateTest<FatMonoGammaTxn> {

    @Override
    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }
}
