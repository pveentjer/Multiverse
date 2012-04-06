package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTxn_initTest extends FatGammaTxn_initTest<FatMonoGammaTxn> {

    @Override
    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }
}
