package org.multiverse.api.exceptions;

/**
 * An {@link IllegalTxnStateException} thrown when an action is executed on a
 * {@link org.multiverse.api.Txn} that is either committed or aborted.
 *
 * @author Peter Veentjer.
 */
public class DeadTxnException extends IllegalTxnStateException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new DeadTxnException.
     */
    public DeadTxnException() {
    }

    /**
     * Creates a new DeadTxnException with the provided message.
     *
     * @param message the message of the exception.
     */
    public DeadTxnException(String message) {
        super(message);
    }

    /**
     * Creates a new DeadTxnException.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public DeadTxnException(String message, Throwable cause) {
        super(message, cause);
    }
}
