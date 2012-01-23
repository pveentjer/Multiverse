package org.multiverse.api.exceptions;

/**
 * A {@link ControlFlowError} thrown when a reading or writing a {@link org.multiverse.api.TransactionalObject}
 * failed, e.g. because it was locked or because a read or write conflict was detected.
 * <p/>
 * A ReadWriteConflict can in most cases be solved by retrying the {@link org.multiverse.api.Transaction} (this will
 * automatically be done by the {@link org.multiverse.api.AtomicBlock}).
 *
 * @author Peter Veentjer.
 */
public class ReadWriteConflict extends ControlFlowError {

    private static final long serialVersionUID = 0;

    public final static ReadWriteConflict INSTANCE = new ReadWriteConflict(false);

    /**
     * Creates a new ReadWriteConflict.
     *
     * @param fillStackTrace if the StackTrace should be filled.
     */
    public ReadWriteConflict(boolean fillStackTrace) {
        super(fillStackTrace);
    }

    /**
     * Creates a new ReadWriteConflict.
     *
     * @param message the message of the ReadWriteConflict.
     */
    public ReadWriteConflict(String message) {
        super(true, message);
    }

    /**
     * Creates a new ReadWriteConflict.
     *
     * @param message the message of the ReadWriteConflict.
     * @param cause   the cause of the ReadWriteConflict.
     */
    public ReadWriteConflict(String message, Throwable cause) {
        super(true, message, cause);
    }
}
