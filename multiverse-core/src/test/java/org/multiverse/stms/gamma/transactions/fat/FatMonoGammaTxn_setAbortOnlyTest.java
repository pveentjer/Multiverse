package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatMonoGammaTxn_setAbortOnlyTest extends FatGammaTxn_setAbortOnlyTest<FatMonoGammaTxn> {
    @Override
    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }

    @Override
    protected FatMonoGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTxn(config);
    }
}
