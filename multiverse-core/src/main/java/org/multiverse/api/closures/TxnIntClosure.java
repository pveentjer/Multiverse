package org.multiverse.api.closures;

import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;

/**
 * A transactional closure contains the logic that needs to be executed transactionally and normally is executed by the
 * {@link TxnExecutor}.
 *
 * This transactional closure is optimized for retuning a primitive type: int. Using this TxnIntClosure instead of
 * the {@link TxnClosure} is that no object wrapper needs to be created and there is no reason to deal with a potential
 * null value.
 *
 * @author Peter Veentjer.
 */
public interface TxnIntClosure{

    /**
     * Executes the closure.
     *
     * @param txn the Transaction. Depending on the txn {@link org.multiverse.api.PropagationLevel}, this could
     *           be null.
     * @return the result of the execution.
     */
     int execute(Txn txn)throws Exception;
}
