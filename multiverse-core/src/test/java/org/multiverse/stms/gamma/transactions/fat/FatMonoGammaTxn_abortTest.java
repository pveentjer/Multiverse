package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.junit.Assert.assertNull;

public class FatMonoGammaTxn_abortTest extends FatGammaTxn_abortTest<FatMonoGammaTxn> {

    @Override
    protected void assertCleaned(FatMonoGammaTxn tx) {
        assertNull(tx.tranlocal.owner);
    }

    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(new GammaTxnConfiguration(stm));
    }

    @Override
    protected FatMonoGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatMonoGammaTxn(config);
    }
}
