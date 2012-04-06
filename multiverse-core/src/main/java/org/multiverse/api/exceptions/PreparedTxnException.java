package org.multiverse.api.exceptions;

/**
 * An {@link IllegalTxnStateException} thrown when an operation is executed on a
 * {@link org.multiverse.api.Txn} while the transaction is prepared.
 *
 * @author Peter Veentjer.
 */
public class PreparedTxnException extends IllegalTxnStateException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new PreparedTxnException with the provided message.
     *
     * @param message the message of the exception.
     */
    public PreparedTxnException(String message) {
        super(message);
    }

    /**
     * Creates a new PreparedTxnException with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public PreparedTxnException(String message, Throwable cause) {
        super(message, cause);
    }
}
