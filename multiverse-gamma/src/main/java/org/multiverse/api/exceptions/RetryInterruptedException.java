package org.multiverse.api.exceptions;

/**
 * A {@link RetryException} thrown when the blocking operation on a {@link org.multiverse.api.Transaction}
 * using the retry has been interrupted.
 *
 * <p>Unlike the {@link InterruptedException} this exception is not checked. A checked interrupted
 * exception is quite nasty to have since either you need to deal with it, or you need to propagate it.
 *
 * <p>When this exception is thrown, the interrupted status on the Thread always is restored.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.TransactionFactoryBuilder#setInterruptible(boolean)
 */
public class RetryInterruptedException extends RetryException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new RetryInterruptedException with the provided message.
     *
     * @param message the message
     */
    public RetryInterruptedException(String message) {
        super(message);
    }

    /**
     * Creates a new RetryInterruptedException with the provided message and cause.
     *
     * @param message the message
     * @param cause   the cause of this exception
     */
    public RetryInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
