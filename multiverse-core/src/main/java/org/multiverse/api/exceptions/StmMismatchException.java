package org.multiverse.api.exceptions;

/**
 * A {@link TransactionExecutionException} thrown when a transaction encounters encounters a transactional object
 * that belongs to a different Stm instance.
 *
 * <p>Normally this exception is not thrown because only a single Stm instance, stored in the {@link org.multiverse.api.GlobalStmInstance}
 * is used.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.GlobalStmInstance
 */
public class StmMismatchException extends TransactionExecutionException {

    private static final long serialVersionUID = 0;
        
    /**
     * Creates a new StmMismatchException with the provided message.
     *
     * @param message the message
     */
    public StmMismatchException(String message) {
        super(message);
    }

    /**
     * Creates a new StmMismatchException with the provided message.
     *
     * @param message the message
     * @param cause the cause
     */
    public StmMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
