package org.multiverse.api.exceptions;


/**
 * An {@link Error} thrown when the state of the {@link org.multiverse.api.Stm} has been compromised. Normally this exception should
 * never happen.
 *
 * @author Peter Veentjer.
 */
public class PanicError extends Error {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new PanicError with the provided message.
     *
     * @param message the message of the PanicError.
     */
    public PanicError(String message) {
        super(message);
    }

    /**
     * Creates a new PanicError with the provided message and cause.
     *
     * @param message the message of the PanicError.
     * @param cause   the cause of the PanicError.
     */
    public PanicError(String message, Throwable cause) {
        super(message, cause);
    }
}
