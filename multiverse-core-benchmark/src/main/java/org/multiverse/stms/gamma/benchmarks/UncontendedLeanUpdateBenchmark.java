package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchyUtils;
import org.multiverse.TestThread;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.stms.gamma.benchmarks.BenchmarkUtils.*;

public class UncontendedLeanUpdateBenchmark implements GammaConstants {
    private GammaStm stm;

    public static void main(String[] args) {
        UncontendedLeanUpdateBenchmark test = new UncontendedLeanUpdateBenchmark();
        test.start(Long.parseLong(args[0]));
    }

    public void start(long transactionCount) {
        int[] processors = generateProcessorRange();

        System.out.printf("Multiverse> Uncontended update lean-transaction benchmark\n");
        System.out.printf("Multiverse> 1 GammaTxnRef per transaction\n");
        System.out.printf("Multiverse> %s Transactions per thread\n", format(transactionCount));
        System.out.printf("Multiverse> Running with the following processor range %s\n", Arrays.toString(processors));
        Result[] result = new Result[processors.length];

        System.out.println("Multiverse> Starting warmup run");
        test(1, transactionCount);
        System.out.println("Multiverse> Finished warmup run");

        long startNs = System.nanoTime();

        for (int k = 0; k < processors.length; k++) {
            int processorCount = processors[k];
            double performance = test(processorCount, transactionCount);
            result[k] = new Result(processorCount, performance);
        }

        long durationNs = System.nanoTime() - startNs;
        System.out.printf("Multiverse> Benchmark took %s seconds\n", TimeUnit.NANOSECONDS.toSeconds(durationNs));

        toGnuplot(result);
    }

    private double test(int threadCount, long transactionsPerThread) {
        System.out.printf("Multiverse> ----------------------------------------------\n");
        System.out.printf("Multiverse> Running with %s thread(s)\n", threadCount);

        stm = new GammaStm();

        UpdateThread[] threads = new UpdateThread[threadCount];

        for (int k = 0; k < threads.length; k++) {
            threads[k] = new UpdateThread(k, transactionsPerThread);
        }

        startAll(threads);
        joinAll(threads);

        long totalDurationMs = 0;
        for (UpdateThread t : threads) {
            totalDurationMs += t.durationMs;
        }

        double transactionsPerSecond = transactionsPerSecondPerThread(
                transactionsPerThread, totalDurationMs, threadCount);
        System.out.printf("Multiverse> Threadcount %s\n", threadCount);
        System.out.printf("Multiverse> Performance %s transactions/second/thread\n",
                BenchyUtils.format(transactionsPerSecond));
        System.out.printf("Multiverse> Performance %s transactions/second\n",
                transactionsPerSecondAsString(transactionsPerThread, totalDurationMs, threadCount));
        return transactionsPerSecond;
    }

    class UpdateThread extends TestThread {
        private final long transactionCount;
        private long durationMs;

        public UpdateThread(int id, long transactionCount) {
            super("UpdateThread-" + id);
            setPriority(Thread.MAX_PRIORITY);
            this.transactionCount = transactionCount;
        }

        public void doRun() {
            GammaTxnLong ref = new GammaTxnLong(stm);

            //FatArrayTreeGammaTxn tx = new FatArrayTreeGammaTxn(stm);
            //FatArrayGammaTxn tx = new FatArrayGammaTxn(stm,1);
            FatMonoGammaTxn tx = new FatMonoGammaTxn(
                    new GammaTxnConfig(stm)
                            .setReadLockMode(LockMode.Exclusive)
                            .setDirtyCheckEnabled(false));
            long startMs = System.currentTimeMillis();
            for (long k = 0; k < transactionCount; k++) {
                ref.openForWrite(tx, LOCKMODE_EXCLUSIVE).long_value++;
                tx.commit();
                tx.hardReset();

                //if (k % 100000000 == 0 && k > 0) {
                //    System.out.printf("%s is at %s\n", getName(), k);
                //}
            }

            assertEquals(transactionCount, ref.atomicGet());

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse> %s is finished in %s ms\n", getName(), durationMs);
        }
    }
}
