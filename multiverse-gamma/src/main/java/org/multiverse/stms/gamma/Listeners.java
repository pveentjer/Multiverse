package org.multiverse.stms.gamma;

import org.multiverse.api.blocking.RetryLatch;

/**
 * A Listeners object contains all the Latches of blockingAllowed transactions that listen to a write on a
 * transactional object. Essentially it is a single linked list.
 * <p/>
 * This is an 'immutable' class. As long as it is registered to a transactional object, it should not be mutated.
 * But as soon as it is removed as listener, only a single thread has access to this Listeners object. This means
 * that it can be pooled.
 * <p/>
 * Setting the Listeners and removing the it should provide the happens before relation so that all changes made
 * to the Listener before it is getAndSet, are visible when it is removed.
 *
 * @author Peter Veentjer
 */
public final class Listeners {
    public Listeners next;
    public RetryLatch listener;
    public long listenerEra;
    //public String threadName;

    /**
     * Prepares this Listeners object for pooling. This is done by:
     * <ol>
     * <li>setting the next to null</li>
     * <li>setting the listener to null</li>
     * <li>setting the listenerEra to Long.MIN_VALUE</li>
     * </ol>
     * <p/>
     * This call is not threadsafe and should only be done by a transaction that has exclusive access to
     * the listeners. The most logical place would be in the object pool when the Listeners is placed there.
     */
    public void prepareForPooling() {
        next = null;
        listener = null;
        listenerEra = Long.MIN_VALUE;
    }

    /**
     * Opens all latches.
     * <p/>
     * All Listeners are put in the pool. The Latches are not put in the pool since no guarantee can be given
     * that the Latch is still registered on a different transactional object.
     * <p/>
     * This call should only be done by the transaction that removed the listeners from
     * the transactional object. So it is not threadsafe,
     *
     * @param pool the GammaObjectPool to store the discarded Listeners in.
     */
    public void openAll(final GammaObjectPool pool) {
        Listeners current = this;
        do {
            Listeners next = current.next;
            current.listener.open(current.listenerEra);
            pool.putListeners(current);
            current = next;
        } while (current != null);
    }

    /**
     * Opens all the listeners. As soon as in the array a null element is found, it signals the end of
     * the list of listeners. This makes is possible to place an array that is larger than the actual
     * number of writes.
     * <p/>
     * The call safely can be made with a null listenersArray. In that case the call is ignored.
     *
     * @param listenersArray the array of Listeners to notify.
     * @param pool           the GammaObjectPool to pool the Listeners and the array containing the listeners.
     */
    public static void openAll(final Listeners[] listenersArray, final GammaObjectPool pool) {
        if (listenersArray == null) {
            return;
        }

        for (int k = 0; k < listenersArray.length; k++) {
            Listeners listeners = listenersArray[k];

            //we can end as soon as a null is found.
            if (listeners == null) {
                break;
            }
            listenersArray[k] = null;
            listeners.openAll(pool);
        }
    }
}
