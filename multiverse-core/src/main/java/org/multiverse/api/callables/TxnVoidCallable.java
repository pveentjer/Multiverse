package org.multiverse.api.callables;

import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;

/**
 * A transactional callable contains the logic that needs to be executed transactionally and normally is executed by the
 * {@link TxnExecutor}.
 *
 * This transactional callable is optimized for returning void. Useful if no value needs to be returned.
 *
 * @author Peter Veentjer.
 */
public interface TxnVoidCallable{

    /**
     * Executes the callable.
     *
     * @param txn the Transaction. Depending on the txn {@link org.multiverse.api.PropagationLevel}, this could
     *           be null.
     * @throws Exception if unable to compute a result
     */
     void call(Txn txn)throws Exception;
}
