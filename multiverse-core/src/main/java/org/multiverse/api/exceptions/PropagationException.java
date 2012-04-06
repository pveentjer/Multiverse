package org.multiverse.api.exceptions;

/**
 * An {@link IllegalStateException} throw when there is a conflict with the {@link org.multiverse.api.Txn}
 * propagation. For more information {@link org.multiverse.api.PropagationLevel}.
 *
 * @author Peter Veentjer.
 */
public class PropagationException extends TxnExecutionException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new PropagationException.
     */
    public PropagationException() {
    }

    /**
     * Creates a new PropagationException with the provided message.
     *
     * @param message the message of the exception.
     */
    public PropagationException(String message) {
        super(message);
    }

    /**
     * Creates a new PropagationException with the provided message and cause.
     *
     * @param message the message of the exception
     * @param cause   the cause of the exception.
     */
    public PropagationException(String message, Throwable cause) {
        super(message, cause);
    }
}
