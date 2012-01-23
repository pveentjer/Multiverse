package org.multiverse.api.exceptions;

/**
 * A {@link ControlFlowError} thrown when an explicit retry is done on a {@link org.multiverse.api.Transaction}.
 * With the {RetryError} it is possible to create blocking transactions.
 *
 * <p>An example is a transaction wants to pop an item from an empty queue. The Retry is caught by the transaction
 * handling logic (e.g the {@link org.multiverse.api.AtomicBlock} and blocks until either a timeout happens or
 * an item is placed on the queue.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.Transaction#retry()
 */
public class RetryError extends ControlFlowError {

    private static final long serialVersionUID = 0;

    public final static RetryError INSTANCE = new RetryError(false);

    /**
     * Creates a new Retry Error.
     *
     * @param fillStackTrace if the StackTrace should be filled.
     */
    public RetryError(boolean fillStackTrace) {
        super(fillStackTrace);
    }
}
