package org.multiverse.api.callables;

import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;

/**
 * A transactional callable contains the logic that needs to be executed transactionally and normally is executed by the
 * {@link TxnExecutor}.
 *
 * This transactional callable is optimized for returning an object reference.
 *
 * @author Peter Veentjer.
 */
public interface TxnCallable<E>{

    /**
     * Executes the callable.
     *
     * @param txn the Transaction. Depending on the txn {@link org.multiverse.api.PropagationLevel}, this could
     *           be null.
     * @return the result of the execution.
     */
     E call(Txn txn)throws Exception;
}
