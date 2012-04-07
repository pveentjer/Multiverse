package org.multiverse.stms.gamma.benchmarks.orec;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GlobalConflictCounter;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

import static org.benchy.BenchyUtils.*;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class OrecCommitLockUpdateDriver extends BenchmarkDriver implements GammaConstants {
    private GammaTxnLong ref;
    private GlobalConflictCounter globalConflictCounter;
    private GammaStm stm;
    private int threadCount;

    private long operationCount = 1000 * 1000 * 1000;
    private UpdateThread[] threads;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Operation count is %s\n", operationCount);
        System.out.printf("Multiverse > Thread count is %s\n", threadCount);

        stm = new GammaStm();
        ref = new GammaTxnLong(stm);
        globalConflictCounter = stm.getGlobalConflictCounter();

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
    public void processResults(TestCaseResult result) {
        long durationMs = result.getDurationMs();
        double transactionsPerSecond = operationsPerSecond(operationCount, durationMs, threadCount);
        double transactionsPerSecondPerThread = operationsPerSecondPerThread(operationCount, durationMs, threadCount);

        result.put("transactionsPerSecond", transactionsPerSecond);
        result.put("transactionsPerSecondPerThread", transactionsPerSecondPerThread);

        System.out.printf("Performance : %s update-cycles/second\n", format(transactionsPerSecond));
        System.out.printf("Performance : %s update-cycles/second/thread\n", format(transactionsPerSecondPerThread));
    }

    class UpdateThread extends TestThread {
        public UpdateThread(int id) {
            super("id-" + id);
        }

        @Override
        public void doRun() throws Exception {
            final long _cycles = operationCount;
            final GammaTxnLong _orec = new GammaTxnLong(stm);

            for (long k = 0; k < _cycles; k++) {
                int arriveStatus = _orec.arriveAndLock(0, LOCKMODE_EXCLUSIVE);
                if ((arriveStatus & MASK_UNREGISTERED) != 0) {
                    _orec.arriveAndLock(0, LOCKMODE_EXCLUSIVE);
                }
                _orec.departAfterUpdateAndUnlock();
            }
        }
    }
}
