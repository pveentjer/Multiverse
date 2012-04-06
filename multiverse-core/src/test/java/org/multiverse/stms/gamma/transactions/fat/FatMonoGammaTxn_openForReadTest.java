package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatMonoGammaTxn_openForReadTest extends FatGammaTxn_openForReadTest<FatMonoGammaTxn> {

    @Override
    protected FatMonoGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTxn(config);
    }

    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(new GammaTxnConfiguration(stm));
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }
}
