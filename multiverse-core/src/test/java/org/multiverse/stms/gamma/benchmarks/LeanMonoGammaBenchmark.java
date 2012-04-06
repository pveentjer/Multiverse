package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchyUtils;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;

public class LeanMonoGammaBenchmark implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void testRead() {
        final long txCount = 5L * 1000 * 1000 * 1000;
        final GammaRef<String> ref1 = new GammaRef<String>(stm);
        final LeanMonoGammaTxn tx = new LeanMonoGammaTxn(stm);

        final long startMs = System.currentTimeMillis();

        for (long k = 0; k < txCount; k++) {
            ref1.openForRead(tx, LOCKMODE_NONE);
            tx.commit();
            tx.hardReset();
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(txCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);
        System.out.println(ref1.toDebugString());
    }

    @Test
    public void testWrite() {
        final long txCount = 1L * 1000 * 1000 * 1000;
        final GammaRef<String> ref1 = new GammaRef<String>(stm);
        final LeanMonoGammaTxn tx = new LeanMonoGammaTxn(stm);

        final long startMs = System.currentTimeMillis();

        for (long k = 0; k < txCount; k++) {
            ref1.openForWrite(tx, LOCKMODE_NONE).ref_value = "foo";
            tx.commit();
            tx.hardReset();
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(txCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);
        System.out.println(ref1.toDebugString());
    }
}
