package org.multiverse.api.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.exceptions.RetryInterruptedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;

public class DefaultRetryLatch_awaitTest {
    @Before
       public void setUp(){
           clearCurrentThreadInterruptedStatus();
       }

    @Test
    public void whenAlreadyOpenAndSameEra(){
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        latch.await(era,"sometransaction");

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

        latch.await(oldEra,"sometransaction");

        assertOpen(latch);
        assertEquals(era, latch.getEra());
    }

    @Test
    public void whenClosedButDifferentEra(){
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.reset();

        long expectedEra = latch.getEra();
        latch.await(era,"sometransaction");

        assertEquals(expectedEra, latch.getEra());
        assertClosed(latch);
    }

    @Test
    public void whenSomeWaitingIsNeeded() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era);
        t.start();

        sleepMs(500);

        assertAlive(t);
        latch.open(era);

        joinAll(t);
        assertOpen(latch);
    }

    @Test
    public void whenStartingInterrupted() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        Thread.currentThread().interrupt();
        try {
            latch.await(era,"sometransaction");
            fail();
        } catch (RetryInterruptedException expected) {
        }

        assertEra(latch, era);
        assertClosed(latch);
    }

    @Test
    public void whenInterruptedWhileWaiting() throws InterruptedException {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era);
        t.setPrintStackTrace(false);
        t.start();

        sleepMs(500);

        assertAlive(t);
        t.interrupt();

        t.join();
        assertClosed(latch);
        assertEra(latch, era);
        t.assertEndedWithInterruptStatus(true);
        t.assertFailedWithException(RetryInterruptedException.class);
    }

    @Test
    public void whenResetWhileWaiting_thenSleepingThreadsNotified() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        AwaitThread t = new AwaitThread(latch, era);
        t.start();

        sleepMs(500);
        assertAlive(t);

        latch.reset();
        joinAll(t);

        assertClosed(latch);
        assertEra(latch, era + 1);
    }

    class AwaitThread extends TestThread {
        private final RetryLatch latch;
        private final long expectedEra;


        AwaitThread(RetryLatch latch, long expectedEra) {
            this.latch = latch;
            this.expectedEra = expectedEra;
        }

        @Override
        public void doRun() throws Exception {
            latch.await(expectedEra,"sometransaction");
        }
    }
}
