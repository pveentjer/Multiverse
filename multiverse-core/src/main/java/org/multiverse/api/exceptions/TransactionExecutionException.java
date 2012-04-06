package org.multiverse.api.exceptions;

/**
 * A {@link RuntimeException} thrown when something fails while executing a {@link org.multiverse.api.Transaction}.
 *
 * <p>This exception is not caught by the {@link org.multiverse.api.TxnExecutor}.
 *
 * @author Peter Veentjer.
 */
public class TransactionExecutionException extends RuntimeException {

    private static final long serialVersionUID = 0;
        
    /**
     * Creates a new TransactionalExecutionException.
     */
    public TransactionExecutionException() {
        super();
    }

    /**
     * Creates a new TransactionalExecutionException with the provided message and cause.
     *
     * @param message message of the exception.
     */
    public TransactionExecutionException(String message) {
        super(message);
    }

    /**
     * Creates a new TransactionalExecutionException with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the Throwable that caused the exception.
     */
    public TransactionExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new TransactionalExecutionException with the provided cause.
     *
     * @param cause the Throwable that was the cause of this TransactionalExecutionException.
     */
    public TransactionExecutionException(Throwable cause) {
        super(cause);
    }
}
