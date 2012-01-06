package org.multiverse.api.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;

public class DefaultRetryLatch_awaitUninterruptibleTest {
    @Before
       public void setUp(){
           clearCurrentThreadInterruptedStatus();
       }

    @Test
    public void whenAlreadyOpenAndSameEra() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        latch.awaitUninterruptible(era);

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

        latch.awaitUninterruptible(oldEra);

        assertOpen(latch);
        assertEquals(era, latch.getEra());
    }

    @Test
    public void whenClosedButDifferentEra() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.reset();

        long expectedEra = latch.getEra();
        latch.awaitUninterruptible(era);

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
    public void whenInterruptedWhileWaiting() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era);
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
    }


    @Test
    public void whenStartingInterrupted() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();

        AwaitThread t = new AwaitThread(latch, era);
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
            latch.awaitUninterruptible(expectedEra);
        }
    }
}
