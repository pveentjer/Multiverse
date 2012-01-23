package org.multiverse.utils;

import org.multiverse.MultiverseConstants;

import java.util.concurrent.locks.LockSupport;

import static java.lang.Thread.yield;

/**
 * Contains some utility functions for shaking out bugs. It can be used by adding this method is the code like
 * this:
 * <pre>
 * if(MultiverseConstants.SHAKE_BUGS){shakeBugs();}
 * </pre>
 * Since the SHAKE_BUGS field is final, it can be removed by the JIT is the bugshaking is disabled so
 * there is no overhead.
 * <p/>
 * At the moment the inside of the Bugshaker is not configurable (so no control on how much delay and how often
 * it happens).
 *
 * @author Peter Veentjer
 */
public final class Bugshaker implements MultiverseConstants {

    /**
     * Delays a random amount of time. What essentially happens is that a random number is selected and one in the
     * n cases, a sleep is done and one in the n cases a yield is done.
     */
    @SuppressWarnings({"CallToThreadYield"})
    public static void shakeBugs() {
        int random = ThreadLocalRandom.current().nextInt(100);

        if (random == 10) {
            sleepUs(10);
        } else if (random == 20) {
            yield();
        }
    }

    /**
     * Delays a number of microseconds. Having a delay smaller than a microsecond doesn't provide
     * value since the minimum delay is a few microseconds.
     *
     * If the delay is smaller than 0, the call is ignored.
     *
     * @param delayUs the number of microseconds to delay.
     */
    public static void sleepUs(long delayUs) {
        if (delayUs <= 0) {
            return;
        }

        long nanos = delayUs * 1000;
        LockSupport.parkNanos(nanos);
    }

    //we don't want any instances
    private Bugshaker() {
    }
}
