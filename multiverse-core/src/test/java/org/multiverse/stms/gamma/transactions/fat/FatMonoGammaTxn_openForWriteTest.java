package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatMonoGammaTxn_openForWriteTest extends FatGammaTxn_openForWriteTest {

    @Override
    protected GammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTxn(config);
    }

    protected GammaTxn newTransaction() {
        return new FatMonoGammaTxn(new GammaTxnConfiguration(stm));
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }


}
