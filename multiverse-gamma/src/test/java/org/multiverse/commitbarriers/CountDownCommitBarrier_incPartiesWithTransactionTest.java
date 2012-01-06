package org.multiverse.commitbarriers;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class CountDownCommitBarrier_incPartiesWithTransactionTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
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
    public void whenNullTransaction_thenNullPointerException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);

        try {
            barrier.incParties(null, 1);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(0, barrier.getNumberWaiting());
        assertEquals(10, barrier.getParties());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionFailure() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);
        Transaction tx = stm.newDefaultTransaction();
        tx.prepare();

        try {
            barrier.incParties(tx, 5);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertEquals(0, barrier.getNumberWaiting());
        assertEquals(10, barrier.getParties());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);

        Transaction tx = stm.newDefaultTransaction();
        tx.abort();

        try {
            barrier.incParties(tx, 1);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertEquals(0, barrier.getNumberWaiting());
        assertEquals(10, barrier.getParties());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);

        Transaction tx = stm.newDefaultTransaction();
        tx.commit();

        try {
            barrier.incParties(tx, 1);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertEquals(0, barrier.getNumberWaiting());
        assertEquals(10, barrier.getParties());
        assertTrue(barrier.isClosed());
    }


    @Test
    public void whenZeroExtraParties() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(5);
        Transaction tx = stm.newDefaultTransaction();
        barrier.incParties(tx, 0);

        assertEquals(5, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenPositiveNumber() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);
        Transaction tx = stm.newDefaultTransaction();
        barrier.incParties(tx, 5);

        assertIsActive(tx);
        assertEquals(0, barrier.getNumberWaiting());
        assertEquals(15, barrier.getParties());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenPartiesAdded_thenAdditionalJoinsNeedToBeExecuted() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(2);

        Transaction tx = stm.newDefaultTransaction();
        barrier.incParties(tx, 1);

        barrier.countDown();
        barrier.countDown();
        assertTrue(barrier.isClosed());
        barrier.countDown();

        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenTransactionAborted_thenPartiesRestored() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(2);

        Transaction tx = stm.newDefaultTransaction();
        barrier.incParties(tx, 10);

        tx.abort();

        assertIsAborted(tx);
        assertTrue(barrier.isClosed());
        assertEquals(2, barrier.getParties());
    }

    @Test
    public void whenPendingTransactions() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(3);

        JoinCommitThread t1 = new JoinCommitThread(stm, barrier);
        JoinCommitThread t2 = new JoinCommitThread(stm, barrier);

        startAll(t1, t2);
        sleepMs(300);

        barrier.atomicIncParties(2);
        sleepMs(300);

        assertAlive(t1, t2);

        assertEquals(2, barrier.getNumberWaiting());
        assertEquals(5, barrier.getParties());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        Transaction tx = stm.newDefaultTransaction();
        try {
            barrier.incParties(tx, 10);
            fail("Should have got CommitBarrierOpenException");
        } catch (CommitBarrierOpenException expected) {
        }

        assertEquals(1, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(0);
        barrier.countDown();

        Transaction tx = stm.newDefaultTransaction();

        try {
            barrier.incParties(tx, 1);
            fail("Should have got CommitBarrierOpenException");
        } catch (CommitBarrierOpenException expected) {
        }

        assertIsActive(tx);
        assertEquals(0, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
        assertTrue(barrier.isCommitted());
    }
}
