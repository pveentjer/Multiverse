package org.multiverse.stms.gamma;

import org.multiverse.api.references.RefFactoryBuilder;

/**
 * A {@link RefFactoryBuilder} tailored for the GammaStm.
 *
 * @author Peter Veentjer.
 */
public interface GammaRefFactoryBuilder extends RefFactoryBuilder {

    @Override
    GammaRefFactory build();
}
