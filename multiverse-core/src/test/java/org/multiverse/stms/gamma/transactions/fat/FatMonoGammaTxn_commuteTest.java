package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatMonoGammaTxn_commuteTest extends FatGammaTxn_commuteTest<FatMonoGammaTxn> {

    @Override
    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(stm);
    }

    @Override
    protected FatMonoGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTxn(config);
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }
}
