package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class CountDownCommitBarrier_tryJoinCommitWithTimeoutTest {
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
    public void whenNullTransaction_thenNullPointerException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        try {
            barrier.tryJoinCommit(null, 1, TimeUnit.DAYS);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenNullTimeout_thenNullPointerException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        Transaction tx = stm.newDefaultTransaction();
        try {
            barrier.tryJoinCommit(tx, 1, null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertIsActive(tx);
        assertTrue(barrier.isClosed());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    @Ignore
    public void whenNotLastOne() {

    }

    @Test
    @Ignore
    public void whenInterruptedWhileWaiting() {

    }

    @Test
    @Ignore
    public void whenTimeout() {

    }

    @Test
    @Ignore
    public void whenAbortedWhileWaiting() {

    }

    @Test
    public void whenCommittedWhileWaiting() throws InterruptedException {
        barrier = new CountDownCommitBarrier(2);

        final GammaIntRef ref = stm.getDefaultRefFactory().newIntRef(0);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
                    @Override
                    public void execute(Transaction tx) throws Exception {
                        ref.getAndIncrement(tx, 1);
                        boolean result = barrier.tryJoinCommit(tx, 1, TimeUnit.DAYS);
                        assertTrue(result);
                    }
                });
            }
        };

        t.start();
        sleepMs(500);

        barrier.countDown();

        t.join();
        assertNothingThrown(t);
        assertTrue(barrier.isCommitted());
        assertEquals(1, ref.atomicGet());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        Transaction tx = stm.newDefaultTransaction();

        try {
            barrier.tryJoinCommit(tx, 1, TimeUnit.DAYS);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertIsActive(tx);
        assertTrue(barrier.isAborted());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() throws InterruptedException {
        barrier = new CountDownCommitBarrier(0);

        Transaction tx = stm.newDefaultTransaction();

        try {
            barrier.tryJoinCommit(tx, 1, TimeUnit.DAYS);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertIsActive(tx);
        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }
}
