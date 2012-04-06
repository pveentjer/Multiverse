package org.multiverse.api;

/**
 * With the PropagationLevel you have control on how the {@link TxnExecutor} reacts on the existence or the non existence
 * of a {@link Transaction}.
 *
 * @author Peter Veentjer.
 * @see TxnFactoryBuilder#setPropagationLevel(PropagationLevel)
 * @see TxnConfiguration#getPropagationLevel()
 */
public enum PropagationLevel {

    /**
     * Indicates that a new transaction always is started, even when there is an active transaction. The
     * active transaction is postponed and used again after the nested transaction commits. It could be that
     * the outer transaction conflicts made on changes by the inner transaction.
     */
    RequiresNew,

    /**
     * Indicates that a new transaction will be used if none exists. If one exists, the logic will lift on that
     * transaction. This is the default propagation level.
     */
    Requires,

    /**
     * Indicates that a transaction should always be available. If not, a
     * {@link org.multiverse.api.exceptions.TransactionMandatoryException} is thrown.
     */
    Mandatory,

    /**
     * Indicates that it the logic can either be run with or without transaction.
     */
    Supports,

    /**
     * Indicates that no active transaction should be available. If a transaction is found,
     * a {@link org.multiverse.api.exceptions.TransactionNotAllowedException} is thrown.
     */
    Never
}
