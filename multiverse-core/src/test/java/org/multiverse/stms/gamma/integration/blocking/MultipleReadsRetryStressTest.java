package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class MultipleReadsRetryStressTest implements GammaConstants {
    private GammaStm stm;
    private GammaTxnLong[] refs;
    private GammaTxnLong stopRef;
    private volatile boolean stop;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        stop = false;
        stopRef = new GammaTxnLong(stm, 0);
    }

    @Test
    public void withMapTransactionAnd2Threads() throws InterruptedException {
        FatVariableLengthGammaTxnFactory txFactory = new FatVariableLengthGammaTxnFactory(stm);
        test(new LeanGammaTxnExecutor(txFactory), 10, 2);
    }

    @Test
    public void withArrayTransactionAnd2Threads() throws InterruptedException {
        int refCount = 10;
        GammaTxnConfig config = new GammaTxnConfig(stm, refCount + 1);
        FatFixedLengthGammaTxnFactory txFactory = new FatFixedLengthGammaTxnFactory(config);
        test(new LeanGammaTxnExecutor(txFactory), refCount, 2);
    }

    @Test
    public void withMapTransactionAnd5Threads() throws InterruptedException {
        FatVariableLengthGammaTxnFactory txFactory = new FatVariableLengthGammaTxnFactory(stm);
        test(new LeanGammaTxnExecutor(txFactory), 10, 5);
    }

    @Test
    public void withArrayTransactionAnd5Threads() throws InterruptedException {
        int refCount = 10;
        GammaTxnConfig config = new GammaTxnConfig(stm, refCount + 1);
        FatFixedLengthGammaTxnFactory txFactory = new FatFixedLengthGammaTxnFactory(config);
        test(new LeanGammaTxnExecutor(txFactory), refCount, 5);
    }

    public void test(TxnExecutor txnExecutor, int refCount, int threadCount) throws InterruptedException {
        refs = new GammaTxnLong[refCount];
        for (int k = 0; k < refs.length; k++) {
            refs[k] = new GammaTxnLong(stm);
        }

        UpdateThread[] threads = new UpdateThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new UpdateThread(k, txnExecutor, threadCount);
        }

        startAll(threads);

        sleepMs(30 * 1000);
        stop = true;

        stopRef.atomicSet(-1);

        System.out.println("Waiting for joining threads");

        joinAll(threads);

        assertEquals(sumCount(threads), sumRefs());
    }

    private long sumRefs() {
        long result = 0;
        for (GammaTxnLong ref : refs) {
            result += ref.atomicGet();
        }
        return result;
    }

    private long sumCount(UpdateThread[] threads) {
        long result = 0;
        for (UpdateThread thread : threads) {
            result += thread.count;
        }
        return result;
    }

    private class UpdateThread extends TestThread {

        private final TxnExecutor txnExecutor;
        private final int id;
        private final int threadCount;
        private long count;

        public UpdateThread(int id, TxnExecutor txnExecutor, int threadCount) {
            super("UpdateThread-" + id);
            this.txnExecutor = txnExecutor;
            this.id = id;
            this.threadCount = threadCount;
        }

        @Override
        public void doRun() {
            TxnVoidClosure closure = new TxnVoidClosure() {
                @Override
                public void call(Txn tx) {
                    if (stopRef.get() < 0) {
                        throw new StopException();
                    }

                    long sum = 0;
                    for (GammaTxnLong ref : refs) {
                        sum += ref.get();
                    }

                    if (sum % threadCount != id) {
                        retry();
                    }

                    GammaTxnLong ref = refs[TestUtils.randomInt(refs.length)];
                    ref.incrementAndGet(1);
                }

            };

            while (!stop) {
                if (count % (10000) == 0) {
                    System.out.println(getName() + " " + count);
                }

                try {
                    txnExecutor.atomic(closure);
                } catch (StopException e) {
                    break;
                }

                count++;
            }
        }
    }

    class StopException extends RuntimeException {
    }
}
