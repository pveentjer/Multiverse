package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.clearCurrentThreadInterruptedStatus;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_constructorTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        clearCurrentThreadInterruptedStatus();
    }

    @After
    public void tearDown() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNegativeParties_thenIllegalArgumentException() {
        new CountDownCommitBarrier(-1);
    }

    @Test
    public void whenZeroParties_thenBarrierCommitted() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(0);
        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenPositiveParties() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(10);
        assertTrue(barrier.isClosed());
        assertEquals(10, barrier.getParties());
        assertEquals(0, barrier.getNumberWaiting());
    }
}
