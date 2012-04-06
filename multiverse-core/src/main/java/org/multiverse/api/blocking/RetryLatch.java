package org.multiverse.api.blocking;

/**
 * A blockingAllowed structure that can be used to create blocking transactions. When a transaction blocks, a
 * 'listener' is added to each read transactional object. This listener is the Latch. Each transactional object
 * can have a set of listeners.
 * <p/>
 * The Latch can safely be created once by a Txn and reused by the same Txn because it works based on
 * an listenerEra. So of opens happen with an older listener-era, the open is ignored. So even though the Latch could
 * be attached to an older ref that didn't get updated, but is updated eventually even though the latch is notified
 * by another ref, there is no problem.
 * <p/>
 * By resetting it, the listenerEra-counter is incremented, so that call to open or await are ignored.
 *
 * @author Peter Veentjer.
 */
public interface RetryLatch {

    /**
     * Checks if the Latch is open.
     *
     * @return true if the Latch is open, false otherwise.
     */
    boolean isOpen();

    /**
     * Opens this latch only if the expectedEra is the same. If the expectedEra is not the same, the call is ignored.
     * If the Latch already is open, this call is also ignored.
     *
     * @param expectedEra the expected era.
     */
    void open(long expectedEra);

    /**
     * Gets the current era.
     *
     * @return the current era.
     */
    long getEra();

    /**
     * Awaits for this latch to open. This call is not responsive to interrupts.
     *
     * @param expectedEra the expected era. If the era is different, the await always succeeds.
     */
    void awaitUninterruptible(long expectedEra);

    /**
     * Awaits for this Latch to open. There are 3 possible ways for this methods to complete;
     * <ol>
     * <li>the era doesn't match the expected era</li>
     * <li>the latch is opened while waiting</li>
     * <li>the latch is interrupted while waiting. When this happens the RetryInterruptedException
     * is thrown and the Thread.interrupt status is restored.</li>
     * </ol>
     *
     * @param expectedEra           the expected era.
     * @param transactionFamilyName the name of the transaction (only needed for creating
     *                              a usable message in the RetryInterruptedException).
     * @throws org.multiverse.api.exceptions.RetryInterruptedException
     *
     */
    void await(long expectedEra, String transactionFamilyName);

    /**
     * Awaits for this latch to open with a timeout. This call is not responsive to interrupts.
     * <p/>
     * When the calling thread is interrupted, the Thread.interrupt status will not be eaten by
     * this method and safely be restored.
     *
     * @param expectedEra  the expected era.
     * @param nanosTimeout the timeout in nanoseconds
     * @return the remaining timeout.  A negative value indicates that the Latch is not opened in time.
     */
    long awaitNanosUninterruptible(long expectedEra, long nanosTimeout);

    /**
     * Awaits for this latch to open with a timeout. This call is responsive to interrupts.
     * <p/>
     * When the calling thread is interrupted, the Thread.interrupt status will not be eaten by
     * this method and safely be restored.
     *
     * @param expectedEra           the expected era
     * @param nanosTimeout          the timeout in nanoseconds. Can safely be called with a zero or negative timeout
     * @param transactionFamilyName the name of the transaction (only needed for creating
     *                              a usable message in the RetryInterruptedException).
     * @return the remaining timeout. A 0 or negative value indicates that the latch is not opened in time.
     * @throws org.multiverse.api.exceptions.RetryInterruptedException
     *
     */
    long awaitNanos(long expectedEra, long nanosTimeout, String transactionFamilyName);

    /**
     * Prepares the Latch for pooling. All waiting threads will be notified and the era is increased.
     */
    void reset();
}
