package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class AtomicGetDriver extends BenchmarkDriver implements GammaConstants {

    private transient GammaStm stm;
    private transient GetThread[] threads;
    private int threadCount;
    private long transactionsPerThread;
    private boolean sharedRef;
    private boolean weakGet;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Threadcount %s\n", threadCount);
        System.out.printf("Multiverse > Transactions/Thread %s \n", transactionsPerThread);
        System.out.printf("Multiverse > WeakGet %s \n", weakGet);
        System.out.printf("Multiverse > SharedRef %s \n", sharedRef);

        stm = new GammaStm();

        threads = new GetThread[threadCount];

        GammaLongRef ref = sharedRef ? new GammaLongRef(stm) : null;
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new GetThread(k, ref == null ? new GammaLongRef(stm) : ref);
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
        for (GetThread t : threads) {
            totalDurationMs += t.durationMs;
        }

        double transactionsPerSecondPerThread = BenchmarkUtils.transactionsPerSecondPerThread(
                transactionsPerThread, totalDurationMs, threadCount);
        double transactionsPerSecond = BenchmarkUtils.transactionsPerSecond(
                transactionsPerThread, totalDurationMs, threadCount);
        System.out.printf("Multiverse > Performance %s transactions/second/thread\n",
                format(transactionsPerSecondPerThread));
        System.out.printf("Multiverse > Performance %s transactions/second\n",
                format(transactionsPerSecond));

        testCaseResult.put("transactionsPerSecondPerThread", transactionsPerSecondPerThread);
        testCaseResult.put("transactionsPerSecond", transactionsPerSecond);
    }

    class GetThread extends TestThread {
        private long durationMs;
        private final GammaLongRef ref;

        public GetThread(int id, GammaLongRef ref) {
            super("AtomicGetThread-" + id);
            setPriority(Thread.MAX_PRIORITY);
            this.ref = ref;
        }

        public void doRun() {
            long startMs = System.currentTimeMillis();
            final long _transactionsPerThread = transactionsPerThread;
            if (weakGet) {
                for (long k = 0; k < _transactionsPerThread; k++) {
                    ref.atomicWeakGet();
                }
            } else {
                for (long k = 0; k < _transactionsPerThread; k++) {
                    ref.atomicGet();
                }
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse > %s is finished in %s ms\n", getName(), durationMs);
        }
    }
}
