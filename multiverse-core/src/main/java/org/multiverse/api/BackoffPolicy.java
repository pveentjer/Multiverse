package org.multiverse.api;

/**
 * A policy to be used when {@link Txn} or an atomicChecked operation can't make any progress, e.g.
 * because there a {@link org.multiverse.api.exceptions.ReadWriteConflict}. If the next attempt would
 * be done without waiting, the contention is going to increase. It can be better to back off to give the
 * contending Transactions some time to complete so that the chance increases that the failing Txn
 * can complete at a next attempt.
 *
 * Of course when there is a lot of contention, the BackoffPolicy isn't going to help and the Txn
 * could start to suffer from a livelock/starvation.
 *
 * @author Peter Veentjer.
 */
public interface BackoffPolicy {

    /**
     * Delays the calling Thread.
     *
     * <p>The implementation is free to make this a no-op call.
     *
     * @param attempt
     * @throws InterruptedException
     */
    void delay(int attempt) throws InterruptedException;

    /**
     * Delays the calling Thread without being interrupted.
     *
     * <p>The implementation is free to make this a no-op call.
     *
     * @param attempt the
     */
    void delayUninterruptible(int attempt);
}
