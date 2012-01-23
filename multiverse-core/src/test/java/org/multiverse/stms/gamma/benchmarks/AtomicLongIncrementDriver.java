package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;

import java.util.concurrent.atomic.AtomicLong;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class AtomicLongIncrementDriver extends BenchmarkDriver {

    private transient IncThread[] threads;
    private int threadCount;
    private long transactionsPerThread;
    private boolean sharedRef;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Threadcount %s\n", threadCount);
        System.out.printf("Multiverse > Transactions/Thread %s \n", transactionsPerThread);
        System.out.printf("Multiverse > SharedRef %s \n", sharedRef);

        threads = new IncThread[threadCount];
        AtomicLong ref = sharedRef ? new AtomicLong() : null;
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new IncThread(k, ref == null ? new AtomicLong() : ref);
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
        for (IncThread t : threads) {
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

    class IncThread extends TestThread {
        private long durationMs;
        private final AtomicLong ref;

        public IncThread(int id, AtomicLong ref) {
            super("IncThread-" + id);
            setPriority(Thread.MAX_PRIORITY);
            this.ref = ref;
        }

        public void doRun() {
            long startMs = System.currentTimeMillis();
            final long _transactionsPerThread = transactionsPerThread;
            for (long k = 0; k < _transactionsPerThread; k++) {
                ref.incrementAndGet();
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse > %s is finished in %s ms\n", getName(), durationMs);
        }
    }
}
