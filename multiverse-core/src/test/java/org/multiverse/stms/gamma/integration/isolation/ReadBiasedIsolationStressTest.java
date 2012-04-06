package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.api.closures.TxnBooleanClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.randomOneOf;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.benchmarks.BenchmarkUtils.transactionsPerSecondAsString;
import static org.multiverse.stms.gamma.benchmarks.BenchmarkUtils.transactionsPerSecondPerThreadAsString;

/**
 * A Stress test that checks if the system is able to deal with mostly reading transactions and doesn't cause
 * any isolation problems.
 *
 * @author Peter Veentjer.
 */
public class ReadBiasedIsolationStressTest {

    private GammaStm stm = (GammaStm) getGlobalStmInstance();
    private int chanceOfUpdate = new GammaTxnLong(stm).getReadBiasedThreshold() * 5;
    private int threadCount = 4;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    //todo: testing with and without arriveenabled functionality

    @Test
    public void none_and_dirtyCheckEnabled() {
        test(LockMode.None, true);
    }

    @Test
    public void none_read_and_dirtyCheckDisabled() {
        test(LockMode.None, false);
    }

    @Test
    public void read_and_dirtyCheckEnabled() {
        test(LockMode.Read, true);
    }

    @Test
    public void read_and_dirtyCheckDisabled() {
        test(LockMode.Read, false);
    }

    @Test
    public void write_and_dirtyCheckEnabled() {
        test(LockMode.Write, true);
    }

    @Test
    public void write_and_dirtyCheckDisabled() {
        test(LockMode.Write, false);
    }

    @Test
    public void commit_and_dirtyCheckEnabled() {
        test(LockMode.Exclusive, true);
    }

    @Test
    public void commit_and_dirtyCheckDisabled() {
        test(LockMode.Exclusive, false);
    }


    public void test(LockMode lockMode, boolean dirtyCheckEnabled) {
        StressThread[] threads = new StressThread[threadCount];
        GammaTxnLong ref = new GammaTxnLong(stm);
        long transactionsPerThread = 100 * 1000 * 1000;

        for (int k = 0; k < threads.length; k++) {
            threads[k] = new StressThread(k, ref, transactionsPerThread, lockMode, dirtyCheckEnabled);
        }

        for (StressThread thread : threads) {
            thread.start();
        }

        joinAll(threads);

        long totalDurationMs = 0;
        long sum = 0;
        for (StressThread thread : threads) {
            totalDurationMs += thread.durationMs;
            sum += thread.incrementCount;
        }

        System.out.println("--------------------------------------------------------");
        System.out.printf("Threadcount:       %s\n", threadCount);
        System.out.printf("Performance:       %s transactions/second/thread\n",
                transactionsPerSecondPerThreadAsString(transactionsPerThread, totalDurationMs, threadCount));
        System.out.printf("Performance:       %s transactions/second\n",
                transactionsPerSecondAsString(transactionsPerThread, totalDurationMs, threadCount));

        assertEquals(sum, ref.atomicGet());
        System.out.println("ref.orec: " + ref.___toOrecString());
    }

    class StressThread extends TestThread {
        private final boolean dirtyCheckEnabled;
        private final GammaTxnLong ref;
        private final long count;
        private long durationMs;
        private LockMode lockMode;
        private long incrementCount = 0;

        public StressThread(int id, GammaTxnLong ref, long count, LockMode lockMode, boolean dirtyCheckEnabled) {
            super("StressThread-" + id);
            this.ref = ref;
            this.count = count;
            this.lockMode = lockMode;
            this.dirtyCheckEnabled = dirtyCheckEnabled;
        }

        @Override
        public void doRun() {
            TxnExecutor executor = stm.newTxnFactoryBuilder()
                    .setDirtyCheckEnabled(dirtyCheckEnabled)
                    .newTxnExecutor();

            TxnBooleanClosure closure = new TxnBooleanClosure() {
                @Override
                public boolean call(Txn tx) throws Exception {
                    ref.getLock().acquire(tx, lockMode);

                    if (randomOneOf(chanceOfUpdate)) {
                        ref.incrementAndGet(tx, 1);
                        return true;
                    } else {
                        ref.get(tx);
                        return false;
                    }
                }
            };

            long startMs = currentTimeMillis();

            for (long k = 0; k < count; k++) {
                if (executor.atomic(closure)) {
                    incrementCount++;
                }

                if (k % 10000000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }

            durationMs = currentTimeMillis() - startMs;

            System.out.printf("finished %s after %s ms\n", getName(), durationMs);
        }
    }
}
