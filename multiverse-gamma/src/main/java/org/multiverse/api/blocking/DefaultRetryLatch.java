package org.multiverse.api.blocking;

import org.multiverse.api.exceptions.RetryInterruptedException;

import static java.lang.String.format;

/**
 * A Cheap {@link RetryLatch} implementation based on the intrinsic lock.
 *
 * @author Peter Veentjer
 */
@SuppressWarnings({"SynchronizeOnThis", "WaitOrAwaitWithoutTimeout"})
public final class DefaultRetryLatch implements RetryLatch {

    private volatile long era = Long.MIN_VALUE;
    private volatile boolean isOpen = false;

     @Override
    public void open(final long expectedEra) {
        if (isOpen || expectedEra != era) {
            return;
        }

        synchronized (this) {
            if (isOpen || expectedEra != era) {
                return;
            }

            isOpen = true;
            notifyAll();
        }
    }

    @Override
    public void await(long expectedEra, String transactionFamilyName) {
        if (isOpen || expectedEra != era) {
            return;
        }

        try {
            synchronized (this) {
                while (!isOpen && era == expectedEra) {
                    wait();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(
                    format("[%s] Was interrupted while waiting on the retry", transactionFamilyName), ex);
        }
    }

    @Override
    public void awaitUninterruptible(final long expectedEra) {
        if (isOpen || expectedEra != era) {
            return;
        }

        boolean restoreInterrupt = false;

        synchronized (this) {
            while (!isOpen && era == expectedEra) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    restoreInterrupt = true;
                }
            }
        }

        if (restoreInterrupt) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public long awaitNanosUninterruptible(final long expectedEra, long nanosTimeout) {
        if (isOpen || expectedEra != era) {
            return nanosTimeout;
        }

        if (nanosTimeout <= 0) {
            return -1;
        }

        boolean restoreInterrupt = false;
        try {
            while (true) {
                long startNs = System.nanoTime();
                try {
                    synchronized (this) {
                        while (!isOpen && expectedEra == era) {
                            if (nanosTimeout <= 0) {
                                return -1;
                            }

                            long ms = nanosTimeout / 1000000;
                            int ns = (int) (nanosTimeout % 1000000);
                            wait(ms, ns);
                            nanosTimeout -= System.nanoTime() - startNs;
                        }

                        return nanosTimeout;
                    }
                } catch (InterruptedException ex) {
                    restoreInterrupt = true;
                    nanosTimeout -= System.nanoTime() - startNs;
                }
            }
        } finally {
            if (restoreInterrupt) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public long awaitNanos(final long expectedEra, long nanosTimeout, String transactionFamilyName) {
        if (isOpen || expectedEra != era) {
            return nanosTimeout;
        }

        if (nanosTimeout <= 0) {
            return -1;
        }

        try {
            synchronized (this) {
                while (!isOpen && expectedEra == era) {
                    if (nanosTimeout <= 0) {
                        return -1;
                    }

                    long ms = nanosTimeout / 1000000;
                    int ns = (int) (nanosTimeout % 1000000);
                    long startNs = System.nanoTime();
                    wait(ms, ns);
                    nanosTimeout -= System.nanoTime() - startNs;
                }

                return nanosTimeout;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(
                    format("[%s] Was interrupted while waiting on the retry", transactionFamilyName), ex);
        }
    }

    @Override
    public long getEra() {
        return era;
    }

    @Override
    public void reset() {
        synchronized (this) {
            if (!isOpen) {
                notifyAll();
            } else {
                isOpen = false;
            }
            era++;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String toString() {
        return format("DefaultRetryLatch(open=%s, era=%s)", isOpen, era);
    }
}
