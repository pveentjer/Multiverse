package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_abortTest {
    private CountDownCommitBarrier barrier;
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
    public void whenNoPartiesWaiting() {
        barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenPartiesWaiting_theyAreAborted() {
        barrier = new CountDownCommitBarrier(3);

        CommitThread thread1 = new CommitThread(barrier);
        CommitThread thread2 = new CommitThread(barrier);
        startAll(thread1, thread2);

        sleepMs(500);
        barrier.abort();
        sleepMs(500);

        assertTrue(barrier.isAborted());
        assertIsAborted(thread1.txn);
        assertIsAborted(thread2.txn);
    }

    class CommitThread extends TestThread {
        final CountDownCommitBarrier barrier;
        Txn txn;

        CommitThread(CountDownCommitBarrier barrier) {
            setPrintStackTrace(false);
            this.barrier = barrier;
        }

        @Override
        public void doRun() throws Exception {
            stm.getDefaultTxnExecutor().execute(new TxnVoidCallable() {
                @Override
                public void call(Txn txn) throws Exception {
                    CommitThread.this.txn = txn;
                    barrier.joinCommitUninterruptibly(txn);
                }
            });
        }
    }

    @Test
    public void whenAborted_thenIgnored() {
        barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        barrier.abort();
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommitted_thenClosedCommitBarrierException() {
        barrier = new CountDownCommitBarrier(0);

        try {
            barrier.abort();
            fail();
        } catch (CommitBarrierOpenException expected) {

        }

        assertTrue(barrier.isCommitted());
    }
}
