package org.multiverse.stms.gamma;

import org.multiverse.api.references.TxnRefFactoryBuilder;

/**
 * A {@link org.multiverse.api.references.TxnRefFactoryBuilder} tailored for the GammaStm.
 *
 * @author Peter Veentjer.
 */
public interface GammaTxnRefFactoryBuilder extends TxnRefFactoryBuilder {

    @Override
    GammaTxnRefFactory build();
}
