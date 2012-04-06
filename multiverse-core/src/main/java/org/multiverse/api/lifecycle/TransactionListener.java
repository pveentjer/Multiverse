package org.multiverse.api.lifecycle;

import org.multiverse.api.Txn;

/**
 * A listener tailored for listening to events in the {@link org.multiverse.api.Txn} life-cycle.
 *
 * @author Peter Veentjer
 * @see TransactionEvent
 * @see org.multiverse.api.TxnConfiguration#getPermanentListeners()
 * @see org.multiverse.api.Txn#register(TransactionListener)
 */
public interface TransactionListener {

    /**
     * Notifies that a certain {@link TransactionEvent} happened inside a {@link org.multiverse.api.Txn}.
     *
     * @param txn the {@link org.multiverse.api.Txn} where the event happened
     * @param e the event
     */
    void notify(Txn txn, TransactionEvent e);
}
