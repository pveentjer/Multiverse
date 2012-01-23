package org.multiverse.api.exceptions;

/**
 * An {@link IllegalTransactionStateException} thrown when an action is executed on a
 * {@link org.multiverse.api.Transaction} that is either committed or aborted.
 *
 * @author Peter Veentjer.
 */
public class DeadTransactionException extends IllegalTransactionStateException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new DeadTransactionException.
     */
    public DeadTransactionException() {
    }

    /**
     * Creates a new DeadTransactionException with the provided message.
     *
     * @param message the message of the exception.
     */
    public DeadTransactionException(String message) {
        super(message);
    }

    /**
     * Creates a new DeadTransactionException.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public DeadTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
