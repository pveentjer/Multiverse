package org.multiverse.api.references;

/**
 * A Builder for creating a {@link RefFactory}. Atm it doesn't provide functionality, but in the future it will
 * contains similar configuration mechanism as the {@link org.multiverse.api.TxnFactoryBuilder}.
 * <p/>
 * A RefFactoryBuilder is considered immutable.
 *
 * @author Peter Veentjer.
 */
public interface RefFactoryBuilder {

    /**
     * Builds a RefFactory.
     *
     * @return the build reference factory.
     */
    RefFactory build();
}
