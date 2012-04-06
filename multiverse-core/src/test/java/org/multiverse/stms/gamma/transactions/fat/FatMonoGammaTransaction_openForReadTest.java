package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatMonoGammaTransaction_openForReadTest extends FatGammaTransaction_openForReadTest<FatMonoGammaTransaction> {

    @Override
    protected FatMonoGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }

    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(new GammaTxnConfiguration(stm));
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }
}
