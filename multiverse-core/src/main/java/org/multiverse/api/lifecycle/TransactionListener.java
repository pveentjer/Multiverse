package org.multiverse.api.lifecycle;

import org.multiverse.api.Transaction;

/**
 * A listener tailored for listening to events in the {@link Transaction} life-cycle.
 *
 * @author Peter Veentjer
 * @see TransactionEvent
 * @see org.multiverse.api.TxnConfiguration#getPermanentListeners()
 * @see Transaction#register(TransactionListener)
 */
public interface TransactionListener {

    /**
     * Notifies that a certain {@link TransactionEvent} happened inside a {@link Transaction}.
     *
     * @param transaction the {@link Transaction} where the event happened
     * @param e the event
     */
    void notify(Transaction transaction, TransactionEvent e);
}
