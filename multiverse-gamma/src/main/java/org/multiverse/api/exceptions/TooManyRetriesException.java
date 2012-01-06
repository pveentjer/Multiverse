package org.multiverse.api.exceptions;

/**
 * A {@link RetryException} thrown when a {@link org.multiverse.api.Transaction} is retried too many times. Uncontrolled
 * retrying could lead to liveness problems like livelocks and starvation.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.TransactionFactoryBuilder#setMaxRetries(int)
 */
public class TooManyRetriesException extends RetryException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new TooManyRetriesException with the provided message.
     *
     * @param message the message of the exception.
     */
    public TooManyRetriesException(String message) {
        super(message);
    }

    /**
     * Creates a new TooManyRetriesException with the provided message.
     *
     * @param message the message of the exception
     * @param cause the cause of the exception
     */
    public TooManyRetriesException(String message, Throwable cause) {
        super(message,cause);
    }
}
