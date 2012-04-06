package org.multiverse.api;

/**
 * An enumeration of all possible states a {@link Txn} can have.
 *
 * @author Peter Veentjer
 */
public enum TxnStatus {

    /**
     * When a Txn is running.
     */
    Active(true),

    /**
     * When the Txn has been checked for conflicts and all resources have been claimed to make sure
     * that a commit is going to succeed.
     */
    Prepared(true),

    /**
     * When a Txn failed to commit.
     */
    Aborted(false),

    /**
     * When a Txn successfully committed.
     */
    Committed(false);

    private final boolean alive;

    TxnStatus(boolean alive) {
        this.alive = alive;
    }

    /**
     * Checks if the Txn still is active/prepared.
     *
     * @return true if the TxnStatus is active or prepared.
     */
    public boolean isAlive() {
        return alive;
    }
}
