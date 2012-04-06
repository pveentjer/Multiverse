package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.assertNull;

public class FatMonoGammaTxn_commitTest extends FatGammaTxn_commitTest<FatMonoGammaTxn> {

    @Override
    protected void assertCleaned(FatMonoGammaTxn transaction) {
        assertNull(transaction.tranlocal.owner);
    }

    @Override
    protected FatMonoGammaTxn newTransaction(GammaTxnConfig config) {
        return new FatMonoGammaTxn(config);
    }

    protected FatMonoGammaTxn newTransaction() {
        return new FatMonoGammaTxn(new GammaTxnConfig(stm));
    }
}
