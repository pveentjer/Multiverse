package org.multiverse.stms.gamma;

import org.multiverse.api.BackoffPolicy;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;

/**
 * An abstract {@link GammaTransactionExecutor} implementation.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractGammaTransactionExecutor implements GammaTransactionExecutor {
    protected final GammaTransactionFactory transactionFactory;
    protected final GammaTransactionConfiguration transactionConfiguration;
    protected final BackoffPolicy backoffPolicy;

    public AbstractGammaTransactionExecutor(final GammaTransactionFactory transactionFactory) {
        if (transactionFactory == null) {
            throw new NullPointerException();
        }
        this.transactionFactory = transactionFactory;
        this.transactionConfiguration = transactionFactory.getConfiguration();
        this.backoffPolicy = transactionConfiguration.backoffPolicy;
    }
}
