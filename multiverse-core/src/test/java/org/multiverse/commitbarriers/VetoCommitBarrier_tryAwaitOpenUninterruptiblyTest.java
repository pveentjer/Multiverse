package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class VetoCommitBarrier_tryAwaitOpenUninterruptiblyTest {

    private VetoCommitBarrier barrier;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        clearCurrentThreadInterruptedStatus();
    }

    @After
    public void tearDown() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test
    public void whenNullTimeout_thenNullPointerException() {
        barrier = new VetoCommitBarrier();

        try {
            barrier.tryAwaitOpenUninterruptibly(1, null);
            fail();
        } catch (NullPointerException expected) {

        }

        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenInterruptedWhileWaiting() throws InterruptedException {
        barrier = new VetoCommitBarrier();

        TestThread t = new TestThread() {
            @Override
            public void doRun() {
                boolean result = barrier.tryAwaitOpenUninterruptibly(1500, TimeUnit.MILLISECONDS);
                assertFalse(result);
            }
        };

        t.start();
        sleepMs(500);
        assertAlive(t);

        t.interrupt();

        t.join();
        assertNothingThrown(t);
        assertTrue(t.hasEndedWithInterruptStatus());
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenCommittedWhileWaiting() throws InterruptedException {
        barrier = new VetoCommitBarrier();

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                boolean result = barrier.tryAwaitOpenUninterruptibly(1, TimeUnit.DAYS);
                assertTrue(result);
            }
        };

        t.start();
        sleepMs(500);
        assertAlive(t);

        barrier.atomicVetoCommit();

        t.join();
        assertNothingThrown(t);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenAbortedWhileWaiting() throws InterruptedException {
        barrier = new VetoCommitBarrier();

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                boolean result = barrier.tryAwaitOpenUninterruptibly(1, TimeUnit.DAYS);
                assertTrue(result);
            }
        };

        t.start();
        sleepMs(500);
        assertAlive(t);

        barrier.abort();

        t.join();
        assertNothingThrown(t);
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenTimeout() throws InterruptedException {
        barrier = new VetoCommitBarrier();

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                boolean result = barrier.tryAwaitOpenUninterruptibly(1, TimeUnit.SECONDS);
                assertFalse(result);
            }
        };

        t.start();
        t.join();
        assertNothingThrown(t);
    }

    @Test
    public void whenCommitted() {
        barrier = new VetoCommitBarrier();
        barrier.atomicVetoCommit();

        boolean result = barrier.tryAwaitOpenUninterruptibly(1, TimeUnit.DAYS);

        assertTrue(result);
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenAborted() {
        barrier = new VetoCommitBarrier();
        barrier.abort();

        boolean result = barrier.tryAwaitOpenUninterruptibly(1, TimeUnit.DAYS);

        assertTrue(result);
        assertTrue(barrier.isAborted());
    }
}
