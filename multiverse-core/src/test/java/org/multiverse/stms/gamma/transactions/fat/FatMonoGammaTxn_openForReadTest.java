package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

public class FatMonoGammaTxn_openForReadTest extends FatGammaTxn_openForReadTest<FatMonoGammaTxn> {

    @Override
    protected FatMonoGammaTxn newTransaction(GammaTxnConfig config) {
        return new FatMonoGammaTxn(config);
    }

    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(new GammaTxnConfig(stm));
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }
}
