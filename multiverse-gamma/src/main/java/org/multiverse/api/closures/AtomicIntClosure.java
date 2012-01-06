package org.multiverse.api.closures;

import org.multiverse.api.Transaction;

/**
* An AtomicClosure tailored for int
*
* @author Peter Veentjer.
*/
public interface AtomicIntClosure{

    /**
     * Executes the closure.
     *
     * @param tx the Transaction. Depending on the TransactionPropagation level, this could
     *           be null.
     * @return the result of the execution.
     * @throws Exception if unable to compute a result
     */
     int execute(Transaction tx)throws Exception;
}
