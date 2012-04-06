package org.multiverse.stms.gamma.transactions;

/**
 * A ThreadLocal that stores the {@link GammaTxnPool}.
 *
 * @author Peter Veentjer.
 */
public final class ThreadLocalGammaTxnPool {

    private final static ThreadLocal<GammaTxnPool> threadlocal = new ThreadLocal<GammaTxnPool>() {
        @Override
        protected GammaTxnPool initialValue() {
            return new GammaTxnPool();
        }
    };


    /**
     * Returns the GammaTxnalPool stored in the ThreadLocalGammaTxnPool. If no instance exists,
     * a new instance is created.
     *
     * @return the GammaTxnPool.
     */
    public static GammaTxnPool getThreadLocalGammaTxnPool() {
        return threadlocal.get();
    }

    //we don't want any instances.

    private ThreadLocalGammaTxnPool() {
    }
}
