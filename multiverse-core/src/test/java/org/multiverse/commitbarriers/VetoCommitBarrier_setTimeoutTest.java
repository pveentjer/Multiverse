package org.multiverse.commitbarriers;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class VetoCommitBarrier_setTimeoutTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullTimeUnit_thenNullPointerException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        try {
            barrier.setTimeout(10, null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTimedOut() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        barrier.setTimeout(500, TimeUnit.MILLISECONDS);
        sleepMs(1000);

        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommittedBeforeTimeout() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        barrier.setTimeout(500, TimeUnit.MILLISECONDS);
        barrier.atomicVetoCommit();

        sleepMs(1000);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenAbortedBeforeTimeout() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        barrier.setTimeout(500, TimeUnit.MILLISECONDS);
        barrier.abort();

        sleepMs(1000);
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.atomicVetoCommit();

        try {
            barrier.setTimeout(10, TimeUnit.SECONDS);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.abort();

        try {
            barrier.setTimeout(10, TimeUnit.SECONDS);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
    }
}
