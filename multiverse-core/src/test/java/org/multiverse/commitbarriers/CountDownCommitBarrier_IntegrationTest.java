package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.references.TxnLong;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_IntegrationTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
        clearCurrentThreadInterruptedStatus();
    }

    @After
    public void tearDown() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test
    public void testMultipleWaiters() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(2);
        AwaitThread t1 = new AwaitThread(barrier);
        t1.start();

        sleepMs(1000);
        assertTrue(t1.isAlive());

        AwaitThread t2 = new AwaitThread(barrier);
        t2.start();

        joinAll(t1, t2);
        assertEquals(0, barrier.getNumberWaiting());
        assertIsCommitted(t2.txn);
        assertIsCommitted(t1.txn);
    }

    @Test
    public void testSingleWaiter() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        AwaitThread t1 = new AwaitThread(barrier);
        t1.start();

        joinAll(t1);
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(barrier.isCommitted());
        assertIsCommitted(t1.txn);
    }

    public class AwaitThread extends TestThread {
        private Txn txn;
        private final CountDownCommitBarrier barrier;


        public AwaitThread(CountDownCommitBarrier barrier) {
            super("AwaitThread");
            this.barrier = barrier;
        }

        @Override
        public void doRun() throws Exception {
            final TxnLong ref = stm.getDefaultRefFactory().newTxnLong(1);

            stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn txn) throws Exception {
                    AwaitThread.this.txn = txn;
                    ref.set(txn, 10);
                    barrier.joinCommitUninterruptibly(txn);
                }
            });
        }
    }
}
