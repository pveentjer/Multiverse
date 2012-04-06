package org.multiverse.stms.gamma;

import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

/**
 * An {@link org.multiverse.api.TransactionExecutor} tailored for the GammaStm.
 *
 * @author  Peter Veentjer.
 */
public interface GammaTransactionExecutor extends TransactionExecutor {

    @Override
    GammaTxnFactory getTransactionFactory();
}
