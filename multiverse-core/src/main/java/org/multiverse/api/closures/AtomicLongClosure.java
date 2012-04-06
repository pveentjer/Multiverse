package org.multiverse.api.closures;

import org.multiverse.api.Txn;

/**
 * An atomic closure tailored for returning a primitive type: long. Using this AtomicLongClosure instead of
 * the {@link AtomicClosure} is that no object wrappers need to be created and there is no reason to deal with a potential
 * null value.
 *
 * @author Peter Veentjer.
 */
public interface AtomicLongClosure{

    /**
     * Executes the closure.
     *
     * @param txn the Transaction. Depending on the txn {@link org.multiverse.api.PropagationLevel}, this could
     *           be null.
     * @return the result of the execution.
     */
     long execute(Txn txn)throws Exception;
}
