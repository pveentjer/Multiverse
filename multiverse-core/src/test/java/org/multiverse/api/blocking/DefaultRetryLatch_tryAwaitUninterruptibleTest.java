package org.multiverse.api.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;

public class DefaultRetryLatch_tryAwaitUninterruptibleTest {

    @Before
    public void setUp() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test
    public void whenAlreadyOpenAndSameEra() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        long result = latch.awaitNanosUninterruptible(era, 10);

        assertEquals(10, result);
        assertOpen(latch);
        assertEquals(era, latch.getEra());
    }

    @Test
    public void whenAlreadyOpenAndDifferentEra() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long oldEra = latch.getEra();
        latch.reset();
        long era = latch.getEra();
        latch.open(era);

        long result = latch.awaitNanosUninterruptible(oldEra, 10);

        assertEquals(10, result);
        assertOpen(latch);
        assertEquals(era, latch.getEra());
    }

    @Test
    public void whenClosedButDifferentEra() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.reset();

        long expectedEra = latch.getEra();
        long result = latch.awaitNanosUninterruptible(era, 10);

        assertEquals(10, result);
        assertEquals(expectedEra, latch.getEra());
        assertClosed(latch);
    }

    @Test
    public void whenSomeWaitingIsNeeded() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era, 10, TimeUnit.SECONDS);
        t.start();

        sleepMs(500);

        assertAlive(t);
        latch.open(era);

        joinAll(t);
        assertOpen(latch);
    }

    @Test
    public void whenTimeout() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era, 1, TimeUnit.SECONDS);
        t.start();
        joinAll(t);

        assertClosed(latch);
        assertEra(latch, era);
        assertTrue(t.result < 0);
    }


    @Test
    public void testAlreadyOpenAndNulTimeout() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        long remaining = latch.awaitNanosUninterruptible(era, 0);

        assertEquals(0, remaining);
        assertOpen(latch);
        assertEra(latch, era);
    }

    @Test
    public void whenStillClosedAndNulTimeout() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        long remaining = latch.awaitNanosUninterruptible(era, 0);

        assertTrue(remaining < 0);
        assertClosed(latch);
        assertEra(latch, era);
    }

    @Test
    public void whenAlreadyOpenAndNegativeTimeout() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        long remaining = latch.awaitNanosUninterruptible(era, -10);

        assertTrue(remaining < 0);
        assertOpen(latch);
        assertEra(latch, era);
    }

    @Test
    public void whenStillClosedAndNegativeTimeout() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        long remaining = latch.awaitNanosUninterruptible(era, -10);

        assertTrue(remaining < 0);
        assertClosed(latch);
        assertEra(latch, era);
    }


    @Test
    public void whenStartingInterrupted() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era, 10, TimeUnit.SECONDS);
        t.setStartInterrupted(true);
        t.start();

        sleepMs(500);
        assertAlive(t);

        //do some waiting and see if it still is waiting
        sleepMs(500);
        assertAlive(t);

        //now lets open the latch
        latch.open(era);

        joinAll(t);
        assertOpen(latch);
        assertEra(latch, era);
        t.assertEndedWithInterruptStatus(true);

        assertTrue(t.result > 0);
        assertTrue(t.result < TimeUnit.SECONDS.toNanos(10));
    }

    @Test
    public void whenInterruptedWhileWaiting() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era, 10, TimeUnit.SECONDS);
        t.start();

        sleepMs(500);

        assertAlive(t);
        t.interrupt();

        //do some waiting and see if it still is waiting
        sleepMs(500);
        assertAlive(t);

        //now lets open the latch
        latch.open(era);

        joinAll(t);
        assertOpen(latch);
        assertEra(latch, era);
        t.assertEndedWithInterruptStatus(true);

        assertTrue(t.result > 0);
        assertTrue(t.result < TimeUnit.SECONDS.toNanos(10));
    }

    @Test
    public void whenResetWhileWaiting_thenSleepingThreadsNotified() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        AwaitThread t = new AwaitThread(latch, era, 10, TimeUnit.SECONDS);
        t.start();

        sleepMs(500);
        assertAlive(t);

        latch.reset();
        joinAll(t);

        assertClosed(latch);
        assertEra(latch, era + 1);
        assertTrue(t.result > 0);
        assertTrue(t.result < TimeUnit.SECONDS.toNanos(10));
    }

    class AwaitThread extends TestThread {
        private final RetryLatch latch;
        private final long expectedEra;
        private long timeout;
        private TimeUnit unit;
        private long result;

        AwaitThread(RetryLatch latch, long expectedEra, long timeout, TimeUnit unit) {
            this.latch = latch;
            this.expectedEra = expectedEra;
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public void doRun() throws Exception {
            result = latch.awaitNanosUninterruptible(expectedEra, unit.toNanos(timeout));
        }
    }
}
