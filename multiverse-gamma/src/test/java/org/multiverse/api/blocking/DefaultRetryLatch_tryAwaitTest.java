package org.multiverse.api.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.exceptions.RetryInterruptedException;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;

public class DefaultRetryLatch_tryAwaitTest {

    @Before
    public void setUp(){
        clearCurrentThreadInterruptedStatus();
    }

       @Test
    public void whenAlreadyOpenAndSameEra(){
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        long result = latch.awaitNanos(era, 10,"sometransaction");

        assertEquals(10, result);
        assertOpen(latch);
        assertEquals(era, latch.getEra());
    }

    @Test
    public void whenAlreadyOpenAndDifferentEra(){
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long oldEra = latch.getEra();
        latch.reset();
        long era = latch.getEra();
        latch.open(era);

        long result = latch.awaitNanos(oldEra, 10,"sometransaction");

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
        long result = latch.awaitNanos(era, 10,"sometransaction");

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
        //assertTrue()
    }

    @Test
    public void testAlreadyOpenAndNulTimeout(){
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        long remaining = latch.awaitNanos(era, 0,"sometransaction");

        assertEquals(0, remaining);
        assertOpen(latch);
        assertEra(latch, era);
    }

    @Test
    public void whenStillClosedAndNulTimeout(){
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        long remaining = latch.awaitNanos(era, 0,"sometransaction");

        assertTrue(remaining <= 0);
        assertClosed(latch);
        assertEra(latch, era);
    }

    @Test
    public void whenAlreadyOpenAndNegativeTimeout(){
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        long remaining = latch.awaitNanos(era, -10,"sometransaction");

        assertTrue(remaining <= 0);
        assertOpen(latch);
        assertEra(latch, era);
    }

    @Test
    public void whenStillClosedAndNegativeTimeout()  {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        long remaining = latch.awaitNanos(era, -10,"sometransaction");

        assertTrue(remaining < 0);
        assertClosed(latch);
        assertEra(latch, era);
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
    public void whenStartingInterrupted_thenTransactionInterruptedExceptionAndInterruptedStatusRestored() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        Thread.currentThread().interrupt();
        try {
            latch.awaitNanos(era, 10,"sometransaction");
            fail();
        } catch (RetryInterruptedException expected) {
        }

        assertTrue(Thread.currentThread().isInterrupted());
        assertEra(latch, era);
        assertClosed(latch);
    }

    @Test
    public void whenInterruptedWhileWaiting_thenTransactionInterruptedExceptionAndInterruptedStatusRestored() throws InterruptedException {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era, 10, TimeUnit.SECONDS);
        t.setPrintStackTrace(false);
        t.start();

        sleepMs(500);

        assertAlive(t);
        t.interrupt();

        t.join();
        assertClosed(latch);
        assertEra(latch, era);
        t.assertFailedWithException(RetryInterruptedException.class);
        t.assertEndedWithInterruptStatus(true);
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
            result = latch.awaitNanos(expectedEra, unit.toNanos(timeout),"sometransaction");
        }
    }
}
