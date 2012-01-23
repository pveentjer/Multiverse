package org.multiverse.api.exceptions;

/**
 * A {@link RuntimeException} thrown when a checked exception is thrown but can't be rethrown.
 * The original checked exception can be retrieved by calling the {@link #getCause()}.
 *
 * @author Peter Veentjer
 */
public class InvisibleCheckedException extends RuntimeException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new InvisibleCheckedException with the given cause.
     *
     * @param cause the cause of the Exception.
     */
    public InvisibleCheckedException(Exception cause) {
        super(cause);
    }

    @Override
    public Exception getCause() {
        return (Exception) super.getCause();
    }
}
