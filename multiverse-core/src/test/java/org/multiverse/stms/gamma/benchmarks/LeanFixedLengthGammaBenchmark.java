package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchyUtils;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxn;

public class LeanFixedLengthGammaBenchmark implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void testRead1() {
        final long txCount = 1000 * 1000 * 1000;
        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm);
        LeanFixedLengthGammaTxn tx = new LeanFixedLengthGammaTxn(stm);

        long startMs = System.currentTimeMillis();

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
    public void testRead2() {
        final long txCount = 1000 * 1000 * 1000;
        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm);
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm);
        LeanFixedLengthGammaTxn tx = new LeanFixedLengthGammaTxn(stm);

        long startMs = System.currentTimeMillis();

        for (long k = 0; k < txCount; k++) {
            ref1.openForRead(tx, LOCKMODE_NONE);
            ref2.openForRead(tx, LOCKMODE_NONE);
            tx.commit();
            tx.hardReset();
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(txCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);


        System.out.println(ref1.toDebugString());
    }

    @Test
    public void testRead3() {
        final long txCount = 1000 * 1000 * 1000;
        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm);
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm);
        GammaTxnRef<String> ref3 = new GammaTxnRef<String>(stm);
        LeanFixedLengthGammaTxn tx = new LeanFixedLengthGammaTxn(stm);

        long startMs = System.currentTimeMillis();

        for (long k = 0; k < txCount; k++) {
            ref1.openForRead(tx, LOCKMODE_NONE);
            ref2.openForRead(tx, LOCKMODE_NONE);
            ref3.openForRead(tx, LOCKMODE_NONE);
            tx.commit();
            tx.hardReset();
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(txCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);


        System.out.println(ref1.toDebugString());
    }
}
