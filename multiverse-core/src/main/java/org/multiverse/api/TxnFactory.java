package org.multiverse.api;

/**
 * A Factory responsible for creating a {@link Transaction}. To set properties on Transactions you need to look
 * at the {@link TxnFactoryBuilder}. It could be that over time different types of transactions are returned,
 * e.g. because the speculative behavior is enabled.
 *
 * <h3>Thread safety</h3>
 *
 * <p>A TxnFactory is thread-safe and it is expected to be shared between threads (doesn't impose it, but it
 * is the most logical use case). It also is expected to be re-used instead of recreated.
 *
 * @author Peter Veentjer.
 * @see TxnFactoryBuilder
 */
public interface TxnFactory {

    /**
     * Gets the {@link TxnConfiguration} used by this TxnFactory.
     *
     * @return the TxnConfiguration.
     */
    TxnConfiguration getConfiguration();

    TxnFactoryBuilder getTransactionFactoryBuilder();

    /**
     * Creates a new {@link Transaction}.
     *
     * @return the created Transaction.
     */
    Transaction newTransaction();
}
