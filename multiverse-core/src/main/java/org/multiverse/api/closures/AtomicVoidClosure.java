package org.multiverse.api.closures;

import org.multiverse.api.Txn;

/**
 * An AtomicClosure that doesn't return a value.
 *
 * @author Peter Veentjer.
 */
public interface AtomicVoidClosure{

    /**
     * Executes the closure.
     *
     * @param txn the Transaction. Depending on the txn {@link org.multiverse.api.PropagationLevel}, this could
     *           be null.
     * @throws Exception if unable to compute a result
     */
     void execute(Txn txn)throws Exception;
}
