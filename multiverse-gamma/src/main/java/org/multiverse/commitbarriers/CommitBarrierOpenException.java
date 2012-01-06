package org.multiverse.commitbarriers;

/**
 * An IllegalStateException that indicates that an operation was executed on the CommitBarrier while it
 * already is opened.
 *
 * @author Peter Veentjer.
 */
public class CommitBarrierOpenException extends IllegalStateException {

    /**
     * Creates a new CommitBarrierOpenException with the provided message.
     *
     * @param message the message
     */
    public CommitBarrierOpenException(String message) {
        super(message);
    }

    /**
     * Creates a new CommitBarrierOpenException with the provided message and cause.
     *
     * @param message the message
     * @param cause the cause
     */
    public CommitBarrierOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
