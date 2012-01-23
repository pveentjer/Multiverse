package org.multiverse.stms.gamma;

/**
 * A ThreadLocal that contains the {@link GammaObjectPool}.
 *
 * @author Peter Veentjer.
 */
public final class ThreadLocalGammaObjectPool {

    public final static ThreadLocal<GammaObjectPool> threadlocal = new ThreadLocal<GammaObjectPool>() {
        protected GammaObjectPool initialValue() {
            return new GammaObjectPool();
        }
    };

    public static GammaObjectPool getThreadLocalGammaObjectPool() {
        return threadlocal.get();
    }

    private ThreadLocalGammaObjectPool() {
    }
}
