package org.multiverse.api.lifecycle;

import org.multiverse.api.Txn;

/**
 * A listener tailored for listening to events in the {@link org.multiverse.api.Txn} life-cycle.
 *
 * @author Peter Veentjer
 * @see TxnEvent
 * @see org.multiverse.api.TxnConfig#getPermanentListeners()
 * @see org.multiverse.api.Txn#register(TxnListener)
 */
public interface TxnListener {

    /**
     * Notifies that a certain {@link TxnEvent} happened inside a {@link org.multiverse.api.Txn}.
     *
     * @param txn the {@link org.multiverse.api.Txn} where the event happened
     * @param e the event
     */
    void notify(Txn txn, TxnEvent e);
}
