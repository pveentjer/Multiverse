package org.multiverse.stms.gamma.transactions;

import org.multiverse.api.TxnFactory;

/**
 * A {@link org.multiverse.api.TxnFactory} tailored for the {@link org.multiverse.stms.gamma.GammaStm}.
 *
 * @author Peter Veentjer.
 */
public interface GammaTxnFactory extends TxnFactory {

    @Override
    GammaTxnConfiguration getConfiguration();

    @Override
    GammaTransaction newTransaction();

    GammaTransaction newTransaction(GammaTransactionPool pool);

    GammaTransaction upgradeAfterSpeculativeFailure(GammaTransaction tx, GammaTransactionPool pool);
}
