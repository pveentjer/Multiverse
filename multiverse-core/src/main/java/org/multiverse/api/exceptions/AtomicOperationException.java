package org.multiverse.api.exceptions;

/**
 * A {@link TxnExecutionException} thrown when an atomicChecked operation has failed (e.g. because the ref
 * was locked).
 *
 * @author Peter Veentjer.
 */
public class AtomicOperationException extends TxnExecutionException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new AtomicOperationException.
     */
    public AtomicOperationException() {
    }

    /**
     * Creates a new AtomicOperationException with the provided message.
     *
     * @param message the message
     */
    public AtomicOperationException(String message) {
        super(message);
    }

    /**
     * Creates a new AtomicOperationException with the provided message and cause.
     *
     * @param message the message
     * @param cause   the cause of the message
     */
    public AtomicOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new AtomicOperationException with the provided message and cause.
     *
     * @param cause the cause of the exception.
     */
    public AtomicOperationException(Throwable cause) {
        super(cause);
    }
}
