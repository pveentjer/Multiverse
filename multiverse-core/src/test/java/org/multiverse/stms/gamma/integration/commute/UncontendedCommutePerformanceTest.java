package org.multiverse.stms.gamma.integration.commute;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.functions.Functions;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.benchmarks.BenchmarkUtils.transactionsPerSecondAsString;

public class UncontendedCommutePerformanceTest {
    private volatile boolean stop;
    private GammaStm stm;
    private GammaTxnLong ref;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaTxnLong(stm);
    }

    @Test
    public void withNormalIncrement() {
        NormalIncThread thread = new NormalIncThread();

        startAll(thread);
        long durationMs = getStressTestDurationMs(30 * 1000);
        sleepMs(durationMs);
        stop = true;
        joinAll(thread);

        long transactionCount = ref.atomicGet();
        String performance = transactionsPerSecondAsString(transactionCount, durationMs);
        System.out.println(performance + " Transactions/second");
    }

    @Test
    public void withCommuteIncrement() {
        CommuteIncThread thread = new CommuteIncThread();

        startAll(thread);
        long durationMs = getStressTestDurationMs(30 * 1000);
        sleepMs(durationMs);
        stop = true;
        joinAll(thread);

        long transactionCount = ref.atomicGet();
        String performance = transactionsPerSecondAsString(transactionCount, durationMs);
        System.out.println(performance + " Transactions/second");
    }

    public class NormalIncThread extends TestThread {
        public NormalIncThread() {
            super("NormalIncThread");
        }

        @Override
        public void doRun() throws Exception {
            TxnExecutor executor = stm.newTxnFactoryBuilder()
                    .setDirtyCheckEnabled(false)
                    .newTxnExecutor();

            TxnVoidClosure closure = new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.openForWrite(btx, LOCKMODE_NONE).long_value++;
                }
            };

            while (!stop) {
                executor.atomic(closure);
            }
        }
    }

    public class CommuteIncThread extends TestThread {
        public CommuteIncThread() {
            super("CommuteIncThread");
        }

        @Override
        public void doRun() throws Exception {
            TxnExecutor executor = stm.newTxnFactoryBuilder()
                    .newTxnExecutor();

            TxnVoidClosure closure = new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.commute(btx, Functions.incLongFunction(1));
                }
            };

            while (!stop) {
                executor.atomic(closure);
            }
        }
    }
}
