package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class ContendedCounterDriver extends BenchmarkDriver {

    private int threadCount;
    private LockMode lockLevel = LockMode.None;
    private long transactionsPerThread;
    private boolean dirtyCheck;
    private GammaStm stm;
    private GammaLongRef ref;
    private IncThread[] threads;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Thread count %s\n", threadCount);
        System.out.printf("Multiverse > Transactions per thread %s\n", transactionsPerThread);
        System.out.printf("Multiverse > Dirty check %s \n", dirtyCheck);
        System.out.printf("Multiverse > Pessimistic lock level %s \n", lockLevel);

        stm = new GammaStm();
        ref = stm.getRefFactoryBuilder().build().newLongRef(0);
        threads = new IncThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new IncThread(k);
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
            totalDurationMs += t.getDurationMs();
        }

        double transactionsPerSecondPerThread = BenchmarkUtils.transactionsPerSecondPerThread(
                transactionsPerThread, totalDurationMs, threadCount);
        double transactionsPerSecond = BenchmarkUtils.transactionsPerSecond(
                transactionsPerThread, totalDurationMs, threadCount);
        System.out.printf("Multiverse > Performance %s transactions/second/thread with %s threads\n",
                format(transactionsPerSecondPerThread), threadCount);
        System.out.printf("Multiverse > Performance %s transactions/second with %s threads\n",
                format(transactionsPerSecond), threadCount);

        testCaseResult.put("transactionsPerSecondPerThread", transactionsPerSecondPerThread);
        testCaseResult.put("transactionsPerSecond", transactionsPerSecond);
    }

    class IncThread extends TestThread {

        public IncThread(int id) {
            super("IncThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            final long _incCount = transactionsPerThread;
            AtomicBlock block = stm.newTransactionFactoryBuilder()
                    .setReadLockMode(lockLevel)
                    .setDirtyCheckEnabled(dirtyCheck)
                    .newAtomicBlock();
            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    ref.increment();
                }
            };

            for (long k = 0; k < _incCount; k++) {
                block.execute(closure);
            }
        }
    }
}
