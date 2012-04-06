package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnInteger;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_joinCommitTest {
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

        Txn tx = stm.newTxnFactoryBuilder()
                .setSpeculative(false)
                .newTransactionFactory()
                .newTxn();

        barrier.joinCommit(tx);

        assertIsCommitted(tx);
        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenAbortedWhileWaiting() throws InterruptedException {
        barrier = new CountDownCommitBarrier(2);

        final GammaTxnInteger ref = stm.getDefaultRefFactory().newTxnInteger(0);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                stm.getDefaultTxnExecutor().atomic(new TxnVoidCallable() {
                    @Override
                    public void call(Txn tx) throws Exception {
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

        final GammaTxnInteger ref = stm.getDefaultRefFactory().newTxnInteger(0);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                stm.getDefaultTxnExecutor().atomicChecked(new TxnVoidCallable() {
                    @Override
                    public void call(Txn tx) throws Exception {
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

        Txn tx = stm.newDefaultTxn();
        tx.commit();

        try {
            barrier.joinCommit(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionAlreadyAborted_thenDeadTxnException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        Txn tx = stm.newDefaultTxn();
        tx.abort();

        try {
            barrier.joinCommit(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        Txn tx = stm.newDefaultTxn();

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

        Txn tx = stm.newDefaultTxn();

        try {
            barrier.joinCommit(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }
}
