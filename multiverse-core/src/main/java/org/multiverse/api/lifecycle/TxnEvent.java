package org.multiverse.api.lifecycle;

/**
 * An enumeration for all possible events for the {@link org.multiverse.api.Txn} life-cycle.
 *
 * @author Peter Veentjer.
 * @see TxnListener
 */
public enum TxnEvent {

    /**
     * Just before starting.
     */
    PreStart,

    /**
     * Just after starting.
     */
    PostStart,

    /**
     * Just before preparing
     */
    PrePrepare,

    /**
     * Just after aborting.
     */
    PostAbort,

    /**
     * Just after committing.
     */
    PostCommit
}
