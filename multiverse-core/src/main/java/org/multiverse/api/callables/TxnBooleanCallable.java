package org.multiverse.api.callables;

import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;

/**
 * A transactional callable contains the logic that needs to be executed transactionally and normally is executed by the
 * {@link TxnExecutor}.
 *
 * This transactional callable is optimized for retuning a primitive type: boolean. Using this TxnBooleanCallable instead of
 * the {@link TxnCallable} is that no object wrapper needs to be created and there is no reason to deal with a potential
 * null value.
 *
 * @author Peter Veentjer.
 */
public interface TxnBooleanCallable{

    /**
     * Executes the callable.
     *
     * @param txn the Transaction. Depending on the txn {@link org.multiverse.api.PropagationLevel}, this could
     *           be null.
     * @return the result of the execution.
     */
     boolean call(Txn txn)throws Exception;
}
