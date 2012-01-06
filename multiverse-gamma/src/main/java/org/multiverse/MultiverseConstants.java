package org.multiverse;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

/**
 * An interface containing global constants. It is a final instead of something mutable so
 * that the JIT can completely remove code if some condition has not been met. The advantage is that you don't have to
 * pay to price for adding some kind of check, if it isn't used. The problem is that the scope is all classes loaded by
 * some classloader, share the same configuration. So one STM implementation with sanity checks enabled and the other
 * not, is not possible.
 * <p/>
 * It is an interface so that is can be 'implemented' for easier access.
 *
 * @author Peter Veentjer
 */
public interface MultiverseConstants {

    /**
     * Indicates of the bugshaker is enabled (for more information see the
     * {@link org.multiverse.utils.Bugshaker}. If disabled, no overhead because it
     * can be removed by the JIT.
     */
    boolean SHAKE_BUGS =
            parseBoolean(getProperty("org.multiverse.bugshaker.enabled", "false"));

    /**
     * Indicates if tracing (so seeing what is going on inside transactions) is
     * enabled. Normally this causes overhead of not used, with this flag the complete
     * tracing logic can be removed by the JIT if disabled).
     */
    boolean TRACING_ENABLED =
            parseBoolean(getProperty("org.multiverse.tracing.enabled", "false"));

    /**
     * Indicates how often the system should yield when it is spinning. When a thread is
     * yielded, it gives the opportunity to another thread to make progress.
     */
    int SPIN_YIELD =
            parseInt(getProperty("org.multiverse.spinYield", "32"));

    final int LOCKMODE_NONE = 0;
    final int LOCKMODE_READ = 1;
    final int LOCKMODE_WRITE = 2;
    final int LOCKMODE_EXCLUSIVE = 3;
}
