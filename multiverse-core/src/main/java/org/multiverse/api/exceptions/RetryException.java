package org.multiverse.api.exceptions;

/**
 * A {@link TxnExecutionException} thrown when retrying a transaction for another attempt fails.
 * E.g. because an explicit retry is not used, or when there are too many retry attempts.
 *
 * @author Peter Veentjer.
 */
public abstract class RetryException extends TxnExecutionException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new RetryException.
     */
    public RetryException() {
    }

    /**
     * Creates a new RetryException with the provided message.
     *
     * @param message the message of the RetryException.
     */
    public RetryException(String message) {
        super(message);
    }

    /**
     * Creates a new RetryException with the provided message and cause.
     *
     * @param message the message of the RetryException.
     * @param cause   the cause of the RetryException.
     */
    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
