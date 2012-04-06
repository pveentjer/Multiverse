package org.multiverse.api;

/**
 * An enumeration of all possible states a {@link Transaction} can have.
 *
 * @author Peter Veentjer
 */
public enum TxnStatus {

    /**
     * When a Transaction is running.
     */
    Active(true),

    /**
     * When the Transaction has been checked for conflicts and all resources have been claimed to make sure
     * that a commit is going to succeed.
     */
    Prepared(true),

    /**
     * When a Transaction failed to commit.
     */
    Aborted(false),

    /**
     * When a Transaction successfully committed.
     */
    Committed(false);

    private final boolean alive;

    TxnStatus(boolean alive) {
        this.alive = alive;
    }

    /**
     * Checks if the Transaction still is active/prepared.
     *
     * @return true if the TxnStatus is active or prepared.
     */
    public boolean isAlive() {
        return alive;
    }
}
