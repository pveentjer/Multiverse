package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class CountDownCommitBarrier_joinCommitTest {
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
    public void whenNullTransaction() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        try {
            barrier.joinCommit(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenLastOneEntering() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        Transaction tx = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .newTransactionFactory()
                .newTransaction();

        barrier.joinCommit(tx);

        assertIsCommitted(tx);
        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenAbortedWhileWaiting() throws InterruptedException {
        barrier = new CountDownCommitBarrier(2);

        final GammaIntRef ref = stm.getDefaultRefFactory().newIntRef(0);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
                    @Override
                    public void execute(Transaction tx) throws Exception {
                        ref.set(tx, 10);
                        barrier.joinCommit(tx);
                    }
                });
            }
        };

        t.setPrintStackTrace(false);
        t.start();

        sleepMs(1000);
        assertAlive(t);
        assertTrue(barrier.isClosed());
        barrier.abort();

        t.join();
        t.assertFailedWithException(IllegalStateException.class);
        assertTrue(barrier.isAborted());
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenCommittedWhileWaiting() {
        barrier = new CountDownCommitBarrier(3);

        JoinCommitThread t1 = new JoinCommitThread(stm, barrier);
        JoinCommitThread t2 = new JoinCommitThread(stm, barrier);

        startAll(t1, t2);
        sleepMs(500);

        barrier.countDown();

        joinAll(t1, t2);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenInterruptedWhileWaiting() throws InterruptedException {
        barrier = new CountDownCommitBarrier(2);

        final GammaIntRef ref = stm.getDefaultRefFactory().newIntRef(0);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                stm.getDefaultAtomicBlock().executeChecked(new AtomicVoidClosure() {
                    @Override
                    public void execute(Transaction tx) throws Exception {
                        ref.set(tx, 10);
                        barrier.joinCommit(tx);
                    }
                });
            }
        };

        t.setPrintStackTrace(false);
        t.start();

        sleepMs(500);
        t.interrupt();

        t.join();
        t.assertFailedWithException(InterruptedException.class);
        assertEquals(0, ref.atomicGet());
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenTransactionAlreadyCommitted() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        Transaction tx = stm.newDefaultTransaction();
        tx.commit();

        try {
            barrier.joinCommit(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionAlreadyAborted_thenDeadTransactionException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        Transaction tx = stm.newDefaultTransaction();
        tx.abort();

        try {
            barrier.joinCommit(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        Transaction tx = stm.newDefaultTransaction();

        try {
            barrier.joinCommit(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(0);

        Transaction tx = stm.newDefaultTransaction();

        try {
            barrier.joinCommit(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }
}
