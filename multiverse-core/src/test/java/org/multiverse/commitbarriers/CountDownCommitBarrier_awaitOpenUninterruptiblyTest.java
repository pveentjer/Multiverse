package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class CountDownCommitBarrier_awaitOpenUninterruptiblyTest {

    private CountDownCommitBarrier barrier;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        clearCurrentThreadInterruptedStatus();
    }

    @After
    public void tearDown() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test
    public void whenStartInterrupted() {
        barrier = new CountDownCommitBarrier(1);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                Thread.currentThread().interrupt();
                barrier.awaitOpenUninterruptibly();
            }
        };
        t.start();

        sleepMs(500);
        assertAlive(t);

        barrier.abort();

        joinAll(t);
    }

    @Test
    public void whenInterruptedWhileWaiting() {
        barrier = new CountDownCommitBarrier(1);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                barrier.awaitOpenUninterruptibly();
            }
        };
        t.start();

        sleepMs(500);
        t.interrupt();

        sleepMs(500);
        assertAlive(t);

        barrier.abort();

        joinAll(t);
    }

    @Test
    public void whenAbortedWhileWaiting() {
        barrier = new CountDownCommitBarrier(1);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                barrier.awaitOpenUninterruptibly();
            }
        };
        t.start();

        sleepMs(500);
        barrier.abort();

        joinAll(t);
    }

    @Test
    public void whenCommittedWhileWaiting() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                barrier.awaitOpenUninterruptibly();
            }
        };
        t.start();

        sleepMs(500);
        barrier.countDown();

        joinAll(t);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenCommitted() throws InterruptedException {
        barrier = new CountDownCommitBarrier(0);

        barrier.awaitOpenUninterruptibly();
        assertTrue(barrier.isCommitted());
        assertEquals(0, barrier.getNumberWaiting());
    }

    @Test
    public void whenAborted() throws InterruptedException {
        barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        barrier.awaitOpenUninterruptibly();
        assertTrue(barrier.isAborted());
        assertEquals(0, barrier.getNumberWaiting());
    }
}
