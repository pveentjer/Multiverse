package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatMonoGammaTransaction_openForWriteTest extends FatGammaTransaction_openForWriteTest {

    @Override
    protected GammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }

    protected GammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(new GammaTxnConfiguration(stm));
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }


}
