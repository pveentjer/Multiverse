package org.multiverse.commitbarriers;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_atomicIncPartiesTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNegativeNumber_thenIllegalArgumentException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);

        try {
            barrier.atomicIncParties(-1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        assertEquals(0, barrier.getNumberWaiting());
        assertEquals(10, barrier.getParties());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenZeroExtraParties() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(5);
        barrier.atomicIncParties(0);

        assertEquals(5, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenPositiveNumber() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);
        barrier.atomicIncParties(5);

        assertEquals(0, barrier.getNumberWaiting());
        assertEquals(15, barrier.getParties());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenPartiesAdded_commitTakesLonger() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(2);
        barrier.atomicIncParties(1);

        barrier.countDown();
        barrier.countDown();
        assertTrue(barrier.isClosed());
        barrier.countDown();
        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenPendingTransactions() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(3);

        JoinCommitThread t1 = new JoinCommitThread(stm, barrier);
        JoinCommitThread t2 = new JoinCommitThread(stm, barrier);

        startAll(t1, t2);

        sleepMs(500);
        assertTrue(barrier.isClosed());

        barrier.atomicIncParties(1);

        sleepMs(500);
        assertAlive(t1, t2);
        assertTrue(barrier.isClosed());
        assertEquals(2, barrier.getNumberWaiting());
        assertEquals(4, barrier.getParties());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        try {
            barrier.atomicIncParties(10);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertEquals(1, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(0);

        try {
            barrier.atomicIncParties();
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertEquals(0, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(barrier.isCommitted());
    }
}
