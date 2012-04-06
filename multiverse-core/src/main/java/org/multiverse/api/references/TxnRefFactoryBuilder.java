package org.multiverse.api.references;

/**
 * A Builder for creating a {@link TxnRefFactory}. Atm it doesn't provide functionality, but in the future it will
 * contains similar configuration mechanism as the {@link org.multiverse.api.TxnFactoryBuilder}.
 * <p/>
 * A TxnRefFactoryBuilder is considered immutable.
 *
 * @author Peter Veentjer.
 */
public interface TxnRefFactoryBuilder {

    /**
     * Builds a TxnRefFactory.
     *
     * @return the build reference factory.
     */
    TxnRefFactory build();
}
