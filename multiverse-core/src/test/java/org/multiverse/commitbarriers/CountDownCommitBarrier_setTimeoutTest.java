package org.multiverse.commitbarriers;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_setTimeoutTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullTimeUnit_thenNullPointerException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);

        try {
            barrier.setTimeout(10, null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTimedOut() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);

        barrier.setTimeout(1000, TimeUnit.MILLISECONDS);
        sleepMs(3000);

        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommittedBeforeTimeout() throws InterruptedException {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);

        barrier.setTimeout(1000, TimeUnit.MILLISECONDS);
        barrier.countDown();

        sleepMs(2000);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenAbortedBeforeTimeout() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);

        barrier.setTimeout(1000, TimeUnit.MILLISECONDS);
        barrier.abort();

        sleepMs(2000);
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(0);

        try {
            barrier.setTimeout(10, TimeUnit.SECONDS);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        try {
            barrier.setTimeout(10, TimeUnit.SECONDS);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
    }
}
