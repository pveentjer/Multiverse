package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTxn_registerTest extends FatGammaTxn_registerTest<FatMonoGammaTxn> {
    @Override
    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }
}
