package org.multiverse.api.closures;

import org.multiverse.api.Transaction;

/**
 * An AtomicClosure tailored for boolean
 * An AtomicClosure that doesn't return a value.
 *
 * @author Peter Veentjer.
 */
public interface AtomicBooleanClosure{

    /**
     * Executes the closure.
     *
     * @param tx the Transaction. Depending on the TransactionPropagation level, this could
     *           be null.
     * @return the result of the execution.
     * @throws Exception if unable to compute a result
     */
     boolean execute(Transaction tx)throws Exception;
}
