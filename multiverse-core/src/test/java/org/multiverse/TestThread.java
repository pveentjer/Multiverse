package org.multiverse;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * A TestThread that tracks if any throwable has been thrown by a thread.
 *
 * @author Peter Veentjer.
 */
public abstract class TestThread extends Thread {

    private final static AtomicLong idGenerator  = new AtomicLong();
    
    private volatile Throwable throwable;
    private volatile Boolean endedWithInterruptStatus;
    private volatile boolean startInterrupted;
    private volatile boolean printStackTrace = true;
    private long durationMs = -1;


    public TestThread() {
        this("TestThread-"+idGenerator.incrementAndGet());
    }

    public TestThread(String name) {
        this(name, false);
    }

    public TestThread(String name, boolean startInterrupted) {
        super(name);
        this.startInterrupted = startInterrupted;
    }

    public void setStartInterrupted(boolean startInterrupted) {
        this.startInterrupted = startInterrupted;
    }

    public void setPrintStackTrace(boolean printStackTrace) {
        this.printStackTrace = printStackTrace;
    }

    public boolean doesStartInterrupted() {
        return startInterrupted;
    }

    public Boolean hasEndedWithInterruptStatus() {
        return endedWithInterruptStatus;
    }

    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public final void run() {
        if (startInterrupted) {
            interrupt();
        }

        long startMs = System.currentTimeMillis();
        try {

            doRun();
        } catch (Throwable ex) {
            if (printStackTrace) {
                System.out.printf("Thread %s has thrown an exception\n", getName());
                ex.printStackTrace();
            }
            this.throwable = ex;
        } finally {
            endedWithInterruptStatus = isInterrupted();
            durationMs = System.currentTimeMillis() - startMs;
        }
    }

    public abstract void doRun() throws Exception;

    public Throwable getThrowable() {
        return throwable;
    }

    public void assertInterrupted() {
        assertFailedWithException(InterruptedException.class);
    }

    public void assertEndedWithInterruptStatus(boolean interrupt) {
        assertEquals(interrupt,endedWithInterruptStatus);
    }

    public void assertFailedWithException(Class expected) {
        assertNotNull(throwable);
        assertTrue("Found exception: " + throwable.getClass().getName(), throwable.getClass().isAssignableFrom(expected));
    }

    public void assertNothingThrown() {
        assertNull(throwable);
    }
}
