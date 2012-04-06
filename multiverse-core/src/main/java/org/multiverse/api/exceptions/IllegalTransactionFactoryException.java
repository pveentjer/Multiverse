package org.multiverse.api.exceptions;

/**
 * An {@link IllegalStateException} thrown when a {@link org.multiverse.api.TxnFactory} can't be created because
 * the {@link org.multiverse.api.TxnConfiguration} is not correct.
 *
 * @author Peter Veentjer.
 */
public class IllegalTransactionFactoryException extends IllegalStateException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new IllegalTransactionFactoryException.
     *
     * @param message the message of the IllegalTransactionFactoryException.
     */
    public IllegalTransactionFactoryException(String message) {
        super(message);
    }

    /**
     * Creates a new IllegalTransactionFactoryException with the provided message and cause.
     *
     * @param message the message of the IllegalTransactionFactoryException.
     * @param cause   the cause of the IllegalTransactionFactoryException
     */
    public IllegalTransactionFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
