package org.multiverse.stms.gamma;

import org.multiverse.api.BackoffPolicy;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

/**
 * An abstract {@link GammaTxnExecutor} implementation.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractGammaTxnExecutor implements GammaTxnExecutor {
    protected final GammaTxnFactory txnFactory;
    protected final GammaTxnConfiguration txnConfiguration;
    protected final BackoffPolicy backoffPolicy;

    public AbstractGammaTxnExecutor(final GammaTxnFactory txnFactory) {
        if (txnFactory == null) {
            throw new NullPointerException();
        }
        this.txnFactory = txnFactory;
        this.txnConfiguration = txnFactory.getConfiguration();
        this.backoffPolicy = txnConfiguration.backoffPolicy;
    }
}
