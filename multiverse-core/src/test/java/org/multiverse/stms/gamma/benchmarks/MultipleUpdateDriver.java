package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransaction;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.stms.gamma.GammaTestUtils.makeReadBiased;

/**
 * @author Peter Veentjer
 */
public class MultipleUpdateDriver extends BenchmarkDriver implements GammaConstants {

    private GammaStm stm;

    private long transactionsPerThread;
    private int refCount;
    private int threadCount;
    private WriteThread[] threads;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Multiple update transaction benchmark\n");
        System.out.printf("Multiverse > Running with %s ref per transaction\n", refCount);
        System.out.printf("Multiverse > %s Transactions per thread\n", format(transactionsPerThread));

        stm = new GammaStm();
        threads = new WriteThread[threadCount];

        for (int k = 0; k < threads.length; k++) {
            threads[k] = new WriteThread(k, refCount);
        }
    }

    @Override
    public void processResults(TestCaseResult testCaseResult) {
        long totalDurationMs = 0;
        for (WriteThread t : threads) {
            totalDurationMs += t.durationMs;
        }

        double transactionsPerSecondPerThread = BenchmarkUtils.transactionsPerSecondPerThread(
                transactionsPerThread, totalDurationMs, threadCount);
        double transactionsPerSecond = BenchmarkUtils.transactionsPerSecond(
                transactionsPerThread, totalDurationMs, threadCount);
        System.out.printf("Multiverse > Performance %s transactions/second with %s threads\n",
                format(transactionsPerSecondPerThread), threadCount);
        System.out.printf("Multiverse > Performance %s transactions/second with %s threads\n",
                format(transactionsPerSecond), threadCount);

        testCaseResult.put("transactionsPerSecondPerThread", transactionsPerSecondPerThread);
        testCaseResult.put("transactionsPerSecond", transactionsPerSecond);
    }

    @Override
    public void run(TestCaseResult testCaseResult) {
        System.out.printf("Multiverse > Running with %s threads\n", threadCount);

        startAll(threads);
        joinAll(threads);
    }

    class WriteThread extends TestThread {
        private final int refCount;
        private long durationMs;

        public WriteThread(int id, int refCount) {
            super("WriteThread-" + id);
            setPriority(Thread.MAX_PRIORITY);
            this.refCount = refCount;
        }

        public void doRun() {
            GammaLongRef[] refs = new GammaLongRef[refCount];
            for (int k = 0; k < refCount; k++) {
                refs[k] = makeReadBiased(new GammaLongRef(stm));
            }

            GammaTxnConfiguration config = new GammaTxnConfiguration(stm, refs.length);

            FatFixedLengthGammaTransaction tx = new FatFixedLengthGammaTransaction(config);

            long startMs = System.currentTimeMillis();

            final long t = transactionsPerThread;
            for (int iteration = 0; iteration < t; iteration++) {
                for (int k = 0; k < refs.length; k++) {
                    refs[k].openForWrite(tx, LOCKMODE_NONE).long_value++;
                }
                tx.commit();
                tx.hardReset();

                if (iteration % 100000000 == 0 && iteration > 0) {
                    System.out.printf("Multiverse > %s is at %s\n", getName(), iteration);
                }
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse > %s is finished in %s ms\n", getName(), durationMs);
        }
    }

}
