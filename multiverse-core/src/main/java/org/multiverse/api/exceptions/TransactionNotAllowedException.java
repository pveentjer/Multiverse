package org.multiverse.api.exceptions;

/**
 * A {@link PropagationException} thrown when a {@link org.multiverse.api.Txn} is found, but is not allowed.
 * A typical cause of this exception is that the {@link org.multiverse.api.PropagationLevel#Never} is used and
 * a transaction is available.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.TxnFactoryBuilder#setPropagationLevel(org.multiverse.api.PropagationLevel)
 */
public class TransactionNotAllowedException extends PropagationException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new NoTransactionAllowedException with the provided message.
     *
     * @param message the message for the exception.
     */
    public TransactionNotAllowedException(String message) {
        super(message);
    }

    /**
     * Creates a new NoTransactionAllowedException with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the Exception.
     */
    public TransactionNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
