package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class CountDownCommitBarrier_joinCommitUninterruptiblyTest {
    private CountDownCommitBarrier barrier;
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
        clearCurrentThreadInterruptedStatus();
    }

    @After
    public void tearDown() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test
    public void whenOpenAndNullTransaction_thenNullPointerException() {
        barrier = new CountDownCommitBarrier(1);

        try {
            barrier.joinCommitUninterruptibly(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenOpenAndThreadAlreadyInterrupted_thenNotInterruptedButInterruptStatusIsSet() {
        barrier = new CountDownCommitBarrier(1);

        Thread.currentThread().interrupt();
        Transaction tx = stm.newDefaultTransaction();

        barrier.joinCommitUninterruptibly(tx);

        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    public void whenOpenAndTransactionActive() {
        Transaction tx = stm.newDefaultTransaction();
        tx.prepare();

        barrier = new CountDownCommitBarrier(1);
        barrier.joinCommitUninterruptibly(tx);

        assertIsCommitted(tx);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenOpenAndTransactionPrepared() {
        Transaction tx = stm.newDefaultTransaction();
        tx.prepare();

        barrier = new CountDownCommitBarrier(1);
        barrier.joinCommitUninterruptibly(tx);

        assertIsCommitted(tx);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenOpenAndLastTransaction_thenAllTransactionsCommitted() {
        barrier = new CountDownCommitBarrier(3);
        AwaitThread t1 = new AwaitThread();
        AwaitThread t2 = new AwaitThread();

        startAll(t1, t2);
        sleepMs(500);

        assertAlive(t1, t2);
        assertTrue(barrier.isClosed());

        Transaction tx = stm.newDefaultTransaction();
        barrier.joinCommitUninterruptibly(tx);
        joinAll(t1, t2);

        assertIsCommitted(tx, t1.tx, t2.tx);
    }

    @Test
    public void whenOpenAndTransactionAborted_thenDeadTransactionException() {
        Transaction tx = stm.newDefaultTransaction();
        tx.abort();

        barrier = new CountDownCommitBarrier(1);
        try {
            barrier.joinCommitUninterruptibly(tx);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertTrue(barrier.isClosed());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenOpenAndTransactionCommitted_thenDeadTransactionException() {
        Transaction tx = stm.newDefaultTransaction();
        tx.commit();

        barrier = new CountDownCommitBarrier(1);
        try {
            barrier.joinCommitUninterruptibly(tx);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertTrue(barrier.isClosed());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() {
        barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        Transaction tx = stm.newDefaultTransaction();

        try {
            barrier.joinCommitUninterruptibly(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() {
        barrier = new CountDownCommitBarrier(1);
        barrier.joinCommitUninterruptibly(stm.newDefaultTransaction());

        Transaction tx = stm.newDefaultTransaction();
        try {
            barrier.joinCommitUninterruptibly(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertIsAborted(tx);
    }

    class AwaitThread extends TestThread {
        private Transaction tx;

        @Override
        public void doRun() throws Exception {
            stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    AwaitThread.this.tx = tx;
                    barrier.joinCommitUninterruptibly(tx);
                }
            });
        }
    }
}
