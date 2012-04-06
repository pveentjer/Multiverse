package org.multiverse.api.exceptions;

/**
 * A {@link TxnExecutionException} thrown when an operation is executed on a
 * {@link org.multiverse.api.Txn} when it is not in a valid state for that operation.
 *
 * @author Peter Veentjer
 */
public class IllegalTxnStateException extends TxnExecutionException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new IllegalTxnStateException.
     */
    public IllegalTxnStateException() {
    }

    /**
     * Creates a new IllegalTxnStateException with the provided message.
     *
     * @param message the message of the exception.
     */
    public IllegalTxnStateException(String message) {
        super(message);
    }

    /**
     * Creates a new IllegalTxnStateException with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public IllegalTxnStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
