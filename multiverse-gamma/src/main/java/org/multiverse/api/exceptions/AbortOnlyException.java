package org.multiverse.api.exceptions;

/**
 * An {@link IllegalTransactionStateException} thrown when a {@link org.multiverse.api.Transaction} is configured
 * as abort only and a prepare/commit is executed.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.Transaction#setAbortOnly()
 * @see org.multiverse.api.Transaction#isAbortOnly()
 */
public class AbortOnlyException extends IllegalTransactionStateException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new AbortOnlyException with the provided message.
     *
     * @param message the message.
     */
    public AbortOnlyException(String message) {
        super(message);
    }

    /**
     * Creates a new AbortOnlyException with the provided message and cause.
     *
     * @param message the message
     * @param cause the cause.
     */
    public AbortOnlyException(String message, Throwable cause) {
        super(message, cause);
    }
}
