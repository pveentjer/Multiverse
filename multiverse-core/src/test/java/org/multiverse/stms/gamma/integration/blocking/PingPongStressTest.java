package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnBooleanCallable;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;


public class PingPongStressTest {

    private volatile boolean stop = false;
    private GammaTxnLong ref;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaTxnLong(stm);
        stop = false;
    }

    @Test
    public void withMonoTransactionAnd2Threads() throws InterruptedException {
        test(new FatMonoGammaTxnFactory(stm), 2);
    }

    @Test
    public void withArrayTransactionAnd2Threads() throws InterruptedException {
        test(new FatFixedLengthGammaTxnFactory(stm), 2);
    }

    @Test
    public void withMapTransactionAnd2Threads() throws InterruptedException {
        test(new FatVariableLengthGammaTxnFactory(stm), 2);
    }

    @Test
    public void withMonoTransactionAnd10Threads() throws InterruptedException {
        test(new FatMonoGammaTxnFactory(stm), 10);
    }

    @Test
    public void withArrayTransactionAnd10Threads() throws InterruptedException {
        test(new FatFixedLengthGammaTxnFactory(stm), 10);
    }

    @Test
    public void withMapTransactionAnd10Threads() throws InterruptedException {
        test(new FatVariableLengthGammaTxnFactory(stm), 10);
    }

    public void test(GammaTxnFactory transactionFactory, int threadCount) throws InterruptedException {
        TxnExecutor executor = new LeanGammaTxnExecutor(transactionFactory);
        PingPongThread[] threads = createThreads(executor, threadCount);

        startAll(threads);

        sleepMs(30 * 1000);
        stop = true;

        stm.getDefaultTxnExecutor().execute(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                ref.set(-abs(ref.get()));
            }
        });

        System.out.println("Waiting for joining threads");
        joinAll(threads);

        assertEquals(sum(threads), -ref.atomicGet());
        System.out.println(stm.getGlobalConflictCounter().count());
    }

    private PingPongThread[] createThreads(TxnExecutor executor, int threadCount) {
        PingPongThread[] threads = new PingPongThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new PingPongThread(k, executor, threadCount);
        }
        return threads;
    }

    private long sum(PingPongThread[] threads) {
        long result = 0;
        for (PingPongThread t : threads) {
            result += t.count;
        }
        return result;
    }

    private class PingPongThread extends TestThread {
        private final TxnExecutor executor;
        private final int threadCount;
        private final int id;
        private long count;

        public PingPongThread(int id, TxnExecutor executor, int threadCount) {
            super("PingPongThread-" + id);
            this.id = id;
            this.executor = executor;
            this.threadCount = threadCount;
        }

        @Override
        public void doRun() {
            TxnBooleanCallable callable = new TxnBooleanCallable() {
                @Override
                public boolean call(Txn tx) throws Exception {
                    if (ref.get() < 0) {
                        return false;
                    }

                    if (ref.get() % threadCount != id) {
                        retry();
                    }

                    ref.increment();
                    return true;
                }
            };

            while (!stop) {
                if (count % (20000) == 0) {
                    System.out.println(getName() + " " + count);
                }

                if (!executor.execute(callable)) {
                    break;
                }
                count++;
            }

            System.out.printf("%s finished\n", getName());
        }
    }
}
