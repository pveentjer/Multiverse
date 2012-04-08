package org.multiverse.api.exceptions;

/**
 * A {@link RuntimeException} thrown when something fails while executing a {@link org.multiverse.api.Txn}.
 *
 * <p>This exception is not caught by the {@link org.multiverse.api.TxnExecutor} because it indicates
 * a programmer error (just like an IllegalArgumentException).
 *
 * @author Peter Veentjer.
 */
public class TxnExecutionException extends RuntimeException {

    private static final long serialVersionUID = 0;
        
    /**
     * Creates a new TransactionalExecutionException.
     */
    public TxnExecutionException() {
        super();
    }

    /**
     * Creates a new TransactionalExecutionException with the provided message and cause.
     *
     * @param message message of the exception.
     */
    public TxnExecutionException(String message) {
        super(message);
    }

    /**
     * Creates a new TransactionalExecutionException with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the Throwable that caused the exception.
     */
    public TxnExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new TransactionalExecutionException with the provided cause.
     *
     * @param cause the Throwable that was the cause of this TransactionalExecutionException.
     */
    public TxnExecutionException(Throwable cause) {
        super(cause);
    }
}
