package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicBooleanClosure;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;


public class PingPongStressTest {

    private volatile boolean stop = false;
    private GammaLongRef ref;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaLongRef(stm);
        stop = false;
    }

    @Test
    public void withMonoTransactionAnd2Threads() throws InterruptedException {
        test(new FatMonoGammaTransactionFactory(stm), 2);
    }

    @Test
    public void withArrayTransactionAnd2Threads() throws InterruptedException {
        test(new FatFixedLengthGammaTransactionFactory(stm), 2);
    }

    @Test
    public void withMapTransactionAnd2Threads() throws InterruptedException {
        test(new FatVariableLengthGammaTransactionFactory(stm), 2);
    }

    @Test
    public void withMonoTransactionAnd10Threads() throws InterruptedException {
        test(new FatMonoGammaTransactionFactory(stm), 10);
    }

    @Test
    public void withArrayTransactionAnd10Threads() throws InterruptedException {
        test(new FatFixedLengthGammaTransactionFactory(stm), 10);
    }

    @Test
    public void withMapTransactionAnd10Threads() throws InterruptedException {
        test(new FatVariableLengthGammaTransactionFactory(stm), 10);
    }

    public void test(GammaTransactionFactory transactionFactory, int threadCount) throws InterruptedException {
        AtomicBlock block = new LeanGammaAtomicBlock(transactionFactory);
        PingPongThread[] threads = createThreads(block, threadCount);

        startAll(threads);

        sleepMs(30 * 1000);
        stop = true;

        stm.getDefaultAtomicBlock().atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                ref.set(-abs(ref.get()));
            }
        });

        System.out.println("Waiting for joining threads");
        joinAll(threads);

        assertEquals(sum(threads), -ref.atomicGet());
        System.out.println(stm.getGlobalConflictCounter().count());
    }

    private PingPongThread[] createThreads(AtomicBlock block, int threadCount) {
        PingPongThread[] threads = new PingPongThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new PingPongThread(k, block, threadCount);
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
        private final AtomicBlock block;
        private final int threadCount;
        private final int id;
        private long count;

        public PingPongThread(int id, AtomicBlock block, int threadCount) {
            super("PingPongThread-" + id);
            this.id = id;
            this.block = block;
            this.threadCount = threadCount;
        }

        @Override
        public void doRun() {
            AtomicBooleanClosure closure = new AtomicBooleanClosure() {
                @Override
                public boolean execute(Transaction tx) throws Exception {
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

                if (!block.atomic(closure)) {
                    break;
                }
                count++;
            }

            System.out.printf("%s finished\n", getName());
        }
    }
}
