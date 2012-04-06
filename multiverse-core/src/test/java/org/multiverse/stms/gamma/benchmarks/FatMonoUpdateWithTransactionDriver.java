package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchyUtils;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.stms.gamma.GammaTxnExecutor;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import static org.junit.Assert.assertEquals;

public class FatMonoUpdateWithTransactionDriver implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public static void main(String[] srgs) {
        FatMonoUpdateWithTransactionDriver driver = new FatMonoUpdateWithTransactionDriver();
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

        final GammaTxnLong ref = new GammaTxnLong(stm, 0);
        long initialVersion = ref.getVersion();

        final GammaTxnExecutor executor = stm.newTxnFactoryBuilder()
                .setFat()
                .setDirtyCheckEnabled(false)
                .setWriteLockMode(writeLockMode)
                .newTxnExecutor();

        final TxnVoidCallable callable = new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                ref.openForWrite((FatMonoGammaTxn) tx, LOCKMODE_NONE).long_value++;
            }
        };

        long startMs = System.currentTimeMillis();

        for (long k = 0; k < txCount; k++) {
            executor.atomic(callable);
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(txCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);

        assertEquals(txCount, ref.long_value);
        assertEquals(txCount + initialVersion, ref.version);
    }
}
