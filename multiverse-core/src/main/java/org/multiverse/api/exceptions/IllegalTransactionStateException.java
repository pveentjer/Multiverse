package org.multiverse.api.exceptions;

/**
 * A {@link TransactionExecutionException} thrown when an operation is executed on a
 * {@link org.multiverse.api.Txn} when it is not in a valid state for that operation.
 *
 * @author Peter Veentjer
 */
public class IllegalTransactionStateException extends TransactionExecutionException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new IllegalTransactionStateException.
     */
    public IllegalTransactionStateException() {
    }

    /**
     * Creates a new IllegalTransactionStateException with the provided message.
     *
     * @param message the message of the exception.
     */
    public IllegalTransactionStateException(String message) {
        super(message);
    }

    /**
     * Creates a new IllegalTransactionStateException with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public IllegalTransactionStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
