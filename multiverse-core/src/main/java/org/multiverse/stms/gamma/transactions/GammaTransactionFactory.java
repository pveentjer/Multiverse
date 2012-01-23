package org.multiverse.stms.gamma.transactions;

import org.multiverse.api.TransactionFactory;

/**
 * A {@link TransactionFactory} tailored for the {@link org.multiverse.stms.gamma.GammaStm}.
 *
 * @author Peter Veentjer.
 */
public interface GammaTransactionFactory extends TransactionFactory {

    @Override
    GammaTransactionConfiguration getConfiguration();

    @Override
    GammaTransaction newTransaction();

    GammaTransaction newTransaction(GammaTransactionPool pool);

    GammaTransaction upgradeAfterSpeculativeFailure(GammaTransaction tx, GammaTransactionPool pool);
}
