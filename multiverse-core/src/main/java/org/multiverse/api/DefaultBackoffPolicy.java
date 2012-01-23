package org.multiverse.api;

import java.util.Random;

import static java.util.concurrent.locks.LockSupport.parkNanos;

/**
 * A {@link DefaultBackoffPolicy} that does an 'exponential' backoff. So each next attempt, the calculated delay is increased
 * and randomized (so the next value can be smaller than the previous, but overall they will increase).
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"CallToThreadYield"})
public final class DefaultBackoffPolicy implements BackoffPolicy {

    public final static BackoffPolicy MAX_100_MS = new DefaultBackoffPolicy();

    private final long minDelayNs;
    private final long[] slotTimes;

    /**
     * Creates an ExponentialBackoffPolicy with 100 nanoseconds as minimal delay and 100 milliseconds as maximum
     * delay.
     */
    public DefaultBackoffPolicy() {
        this(1000);
    }

    /**
     * Creates an ExponentialBackoffPolicy with given maximum delay.
     *
     * @param minDelayNs the minimum delay in nanoseconds to wait. If a negative or zero value provided, it will be
     *                   interpreted that no external minimal value is needed.
     * @throws NullPointerException if unit is null.
     */
    public DefaultBackoffPolicy(long minDelayNs) {
        Random random = new Random();
        slotTimes = new long[1000];
        this.minDelayNs = minDelayNs;

        double a = 100;
        double b = -4963;
        for (int k = 0; k < slotTimes.length; k++) {
            slotTimes[k] = Math.round(random.nextDouble() * f(k, a, b));
        }
    }

    private int f(int x, double a, double b) {
        int result = (int) Math.round(a * x * x + b * x);
        return result < 0 ? 0 : result;
    }

    @Override
    public void delay(int attempt) throws InterruptedException {
        delayUninterruptible(attempt);
    }

    @Override
    public void delayUninterruptible(int attempt) {
        long delayNs = calcDelayNs(attempt);

        if (delayNs >= minDelayNs) {
            parkNanos(delayNs);
        } else if (attempt % 20 == 0) {
            Thread.yield();
        }
    }

    protected long calcDelayNs(int attempt) {
        int slotIndex = attempt >= slotTimes.length ? slotTimes.length - 1 : attempt;
        return slotTimes[slotIndex];
    }
}

