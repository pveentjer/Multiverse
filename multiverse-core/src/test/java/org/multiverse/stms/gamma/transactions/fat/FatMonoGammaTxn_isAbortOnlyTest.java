package org.multiverse.stms.gamma.transactions.fat;

public class FatMonoGammaTxn_isAbortOnlyTest extends FatGammaTxn_isAbortOnlyTest<FatMonoGammaTxn> {

    @Override
    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }
}
