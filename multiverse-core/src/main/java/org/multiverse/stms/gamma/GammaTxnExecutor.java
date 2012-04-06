package org.multiverse.stms.gamma;

import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

/**
 * An {@link org.multiverse.api.TxnExecutor} tailored for the GammaStm.
 *
 * @author  Peter Veentjer.
 */
public interface GammaTxnExecutor extends TxnExecutor {

    @Override
    GammaTxnFactory getTxnFactory();
}
