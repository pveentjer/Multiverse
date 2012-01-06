package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransaction;

import static org.benchy.BenchyUtils.format;
import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;

public class UncontendedMonoUpdateDriver extends BenchmarkDriver {

    private GammaStm stm;
    private UpdateThread[] threads;

    private int threadCount = 1;
    private int transactionsPerThread = 100 * 1000 * 1000;
    private boolean dirtyCheck = false;
    private LockMode lockMode = LockMode.None;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Thread count %s \n", threadCount);
        System.out.printf("Multiverse > Transactions per thread %s \n", transactionsPerThread);
        System.out.printf("Multiverse > Dirtycheck %s \n", dirtyCheck);
        System.out.printf("Multiverse > Locklevel %s \n", lockMode);

        stm = new GammaStm();

        threads = new UpdateThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new UpdateThread(k);
        }
    }

    @Override
    public void run(TestCaseResult testCaseResult) {
        startAll(threads);
        joinAll(threads);
    }

    @Override
    public void processResults(TestCaseResult testCaseResult) {
        long totalDurationMs = 0;
        for (UpdateThread t : threads) {
            totalDurationMs += t.durationMs;
        }

        double transactionsPerSecondPerThread = BenchmarkUtils.transactionsPerSecondPerThread(
                transactionsPerThread, totalDurationMs, threadCount);
        double transactionsPerSecond = BenchmarkUtils.transactionsPerSecond(transactionsPerThread, totalDurationMs, threadCount);
        System.out.printf("Multiverse > Performance %s transactions/second/thread\n",
                format(transactionsPerSecondPerThread));
        System.out.printf("Multiverse > Performance %s transactions/second\n",
                format(transactionsPerSecond));

        testCaseResult.put("transactionsPerSecondPerThread", transactionsPerSecondPerThread);
        testCaseResult.put("transactionsPerSecond", transactionsPerSecond);
    }

    class UpdateThread extends TestThread {
        private long durationMs;

        public UpdateThread(int id) {
            super("UpdateThread-" + id);
        }

        public void doRun() {
            final GammaLongRef ref = new GammaLongRef(stm);

            FatMonoGammaTransaction tx = new FatMonoGammaTransaction(
                    new GammaTransactionConfiguration(stm)
                            .setReadLockMode(lockMode)
                            .setDirtyCheckEnabled(dirtyCheck));
            long startMs = System.currentTimeMillis();
            final long _transactionCount = transactionsPerThread;
            for (long k = 0; k < _transactionCount; k++) {
                ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
                tx.commit();
                tx.hardReset();
            }

            assertEquals(transactionsPerThread, ref.atomicGet());

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse > %s is finished in %s ms\n", getName(), durationMs);
        }
    }
}
