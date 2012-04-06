package org.multiverse.api.exceptions;

/**
 * An {@link IllegalStateException} thrown when a {@link org.multiverse.api.TxnFactory} can't be created because
 * the {@link org.multiverse.api.TxnConfig} is not correct.
 *
 * @author Peter Veentjer.
 */
public class IllegalTxnFactoryException extends IllegalStateException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new IllegalTxnFactoryException.
     *
     * @param message the message of the IllegalTxnFactoryException.
     */
    public IllegalTxnFactoryException(String message) {
        super(message);
    }

    /**
     * Creates a new IllegalTxnFactoryException with the provided message and cause.
     *
     * @param message the message of the IllegalTxnFactoryException.
     * @param cause   the cause of the IllegalTxnFactoryException
     */
    public IllegalTxnFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
