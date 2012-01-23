package org.multiverse.stms.gamma;

import org.multiverse.api.BackoffPolicy;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;

/**
 * An abstract {@link GammaAtomicBlock} implementation.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractGammaAtomicBlock implements GammaAtomicBlock {
    protected final GammaTransactionFactory transactionFactory;
    protected final GammaTransactionConfiguration transactionConfiguration;
    protected final BackoffPolicy backoffPolicy;

    public AbstractGammaAtomicBlock(final GammaTransactionFactory transactionFactory) {
        if (transactionFactory == null) {
            throw new NullPointerException();
        }
        this.transactionFactory = transactionFactory;
        this.transactionConfiguration = transactionFactory.getConfiguration();
        this.backoffPolicy = transactionConfiguration.backoffPolicy;
    }
}
