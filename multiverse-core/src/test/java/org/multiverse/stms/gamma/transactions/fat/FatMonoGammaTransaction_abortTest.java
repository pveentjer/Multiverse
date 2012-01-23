package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

import static org.junit.Assert.assertNull;

public class FatMonoGammaTransaction_abortTest extends FatGammaTransaction_abortTest<FatMonoGammaTransaction> {

    @Override
    protected void assertCleaned(FatMonoGammaTransaction tx) {
        assertNull(tx.tranlocal.owner);
    }

    protected FatMonoGammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(new GammaTransactionConfiguration(stm));
    }

    @Override
    protected FatMonoGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }
}
