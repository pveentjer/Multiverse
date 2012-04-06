package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class MonoReadDriver extends BenchmarkDriver implements GammaConstants {

    private GammaStm stm;
    private ReadThread[] threads;
    private int threadCount;
    private long transactionsPerThread;
    private int lockMode = LOCKMODE_NONE;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Thread count is %s\n", threadCount);
        System.out.printf("Multiverse > Transactions/thread is %s\n", transactionsPerThread);

        stm = new GammaStm();

        threads = new ReadThread[threadCount];

        for (int k = 0; k < threads.length; k++) {
            threads[k] = new ReadThread(k, transactionsPerThread);
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
        for (ReadThread t : threads) {
            totalDurationMs += t.durationMs;
        }

        double transactionsPerSecondPerThread = BenchmarkUtils.transactionsPerSecondPerThread(
                transactionsPerThread, totalDurationMs, threadCount);
        double transactionsPerSecond = BenchmarkUtils.transactionsPerSecond(transactionsPerThread, totalDurationMs, threadCount);
        System.out.printf("Multiverse > Performance %s transactions/second/thread\n",
                format(transactionsPerSecondPerThread));
        System.out.printf("Multiverse > Performance %s transactions/second\n",
                format(transactionsPerSecond));

        testCaseResult.put("transactionsPerSecond", transactionsPerSecond);
        testCaseResult.put("transactionsPerSecondPerThread", transactionsPerSecondPerThread);
    }

    class ReadThread extends TestThread {
        private final long transactionCount;
        private long durationMs;

        public ReadThread(int id, long transactionCount) {
            super("ReadThread-" + id);
            this.transactionCount = transactionCount;
        }

        public void doRun() {
            GammaLongRef ref = new GammaLongRef(stm);

            FatMonoGammaTxn tx = new FatMonoGammaTxn(
                    new GammaTxnConfiguration(stm)
                            .setReadLockMode(LockMode.Exclusive)
                            .setDirtyCheckEnabled(false));
            long startMs = System.currentTimeMillis();
            final long _transactionCount = transactionCount;
            for (long k = 0; k < _transactionCount; k++) {
                ref.openForRead(tx, lockMode);
                tx.commit();
                tx.hardReset();
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse > %s is finished in %s ms\n", getName(), durationMs);
        }
    }
}

