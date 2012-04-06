package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxnFactory;

/**
 * The refCount in some cases is set to an unrealistic high value because
 * normally you want to have a 10/20 refs inside max since a full conflict
 * scan needs to be done. But it is a nice way to check if it still is able
 * to deal with read consistency.
 */
public class RefReadConsistency_LeanFixedLengthGammaTransaction_StressTest extends RefReadConsistency_AbstractTest {

    private int refCount;

    @Test
    public void testWith2Refs() {
        refCount = 2;
        run(refCount);
    }

    @Test
    public void testWith4Refs() {
        refCount = 4;
        run(refCount);
    }

    @Test
    public void testWith8Refs() {
        refCount = 8;
        run(refCount);
    }

    @Test
    public void testWith16Refs() {
        refCount = 16;
        run(refCount);
    }

    @Test
    public void testWith32Refs() {
        refCount = 32;
        run(refCount);
    }

    @Test
    public void testWith64Refs() {
        refCount = 64;
        run(refCount);
    }

    @Test
    public void testWith128Refs() {
        refCount = 128;
        run(refCount);
    }

    @Test
    public void testWith512Refs() {
        refCount = 512;
        run(refCount);
    }

    @Override
    protected TransactionExecutor createReadBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm, refCount)
                .setMaximumPoorMansConflictScanLength(refCount)
                .setMaxRetries(10000);
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(config));
    }

    @Override
    protected TransactionExecutor createWriteBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm, refCount)
                .setMaximumPoorMansConflictScanLength(refCount)
                .setMaxRetries(10000);
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(config));
    }
}
