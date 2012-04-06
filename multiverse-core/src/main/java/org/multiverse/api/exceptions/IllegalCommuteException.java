package org.multiverse.api.exceptions;

/**
 * A {@link TxnExecutionException} thrown when {@link org.multiverse.api.Txn} access is done while
 * a commuting function is being evaluated.
 *
 * <p>The reason why Txn access is not allowed,  is that once other reads/writes are done while executing the commuting
 * behavior, you can have read/write inconsistencies. E.g. in Clojure the same commuting function can be executed more than
 * during the execution of a transaction once on a reference, leading to different values every time executed (e.g. the value it
 * already had inside the transaction, and the most recent committed value when the commuting operation is calculated during
 * transaction commit.
 *
 * @author Peter Veentjer.
 */
public class IllegalCommuteException extends TxnExecutionException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new IllegalCommuteException with the provided message.
     *
     * @param message the message
     */
    public IllegalCommuteException(String message) {
        super(message);
    }

    /**
     * Creates a new IllegalCommuteException with the provided message and cause.
     *
     * @param message the message
     * @param cause   the cause.
     */
    public IllegalCommuteException(String message, Throwable cause) {
        super(message, cause);
    }
}
