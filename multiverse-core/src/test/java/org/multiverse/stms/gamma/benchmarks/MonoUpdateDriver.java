package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchyUtils;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import static org.junit.Assert.assertEquals;

public class MonoUpdateDriver implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public static void main(String[] srgs) {
        MonoUpdateDriver driver = new MonoUpdateDriver();
        driver.setUp();
        driver.testNoLocking();
    }

    @Test
    public void testNoLocking() {
        test(LockMode.None);
    }

    @Test
    public void testReadLock() {
        test(LockMode.Read);
    }

    @Test
    public void testWriteLock() {
        test(LockMode.Write);
    }

    @Test
    public void testExclusiveLock() {
        test(LockMode.Exclusive);
    }

    public void test(LockMode writeLockMode) {
        final long txCount = 1000 * 1000 * 1000;

        final FatMonoGammaTxn tx = new FatMonoGammaTxn(
                new GammaTxnConfiguration(stm)
                        .setDirtyCheckEnabled(false)
                        .setWriteLockMode(writeLockMode));

        final GammaLongRef ref = new GammaLongRef(stm, 0);
        long initialVersion = ref.getVersion();

        long startMs = System.currentTimeMillis();
        for (long k = 0; k < txCount; k++) {
            ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
            tx.commit();
            tx.hardReset();
        }
        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(txCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);

        assertEquals(txCount, ref.long_value);
        assertEquals(txCount + initialVersion, ref.version);
    }
}
