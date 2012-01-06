package org.multiverse.api.exceptions;

/**
 * A {@link RetryException} throw when a {@link org.multiverse.api.StmUtils#retry()} or {@link org.multiverse.api.Transaction#retry()}
 * done on a {@link org.multiverse.api.Transaction} without the possibility of progress, for example when the readset of a transaction
 * is empty.
 *
 * @author Peter Veentjer.
 */
public class RetryNotPossibleException extends RetryException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new NoRetryPossibleException with the provided message.
     *
     * @param message the message of the exception.
     */
    public RetryNotPossibleException(String message) {
        super(message);
    }

    /**
     * Creates a new NoRetryPossibleException with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public RetryNotPossibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
