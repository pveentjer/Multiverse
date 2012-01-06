package org.multiverse.stms.gamma.transactions;

/**
 * A ThreadLocal that stores the {@link GammaTransactionPool}.
 *
 * @author Peter Veentjer.
 */
public final class ThreadLocalGammaTransactionPool {

    private final static ThreadLocal<GammaTransactionPool> threadlocal = new ThreadLocal<GammaTransactionPool>() {
        @Override
        protected GammaTransactionPool initialValue() {
            return new GammaTransactionPool();
        }
    };


    /**
     * Returns the GammaTransactionalPool stored in the ThreadLocalGammaTransactionPool. If no instance exists,
     * a new instance is created.
     *
     * @return the GammaTransactionPool.
     */
    public static GammaTransactionPool getThreadLocalGammaTransactionPool() {
        return threadlocal.get();
    }

    //we don't want any instances.

    private ThreadLocalGammaTransactionPool() {
    }
}
