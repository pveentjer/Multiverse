package org.multiverse.stms.gamma;

import org.multiverse.api.references.TxnRefFactory;
import org.multiverse.stms.gamma.transactionalobjects.*;

/**
 * A {@link org.multiverse.api.references.TxnRefFactory} tailored for the GammaStm.
 *
 * @author Peter Veentjer.
 */
public interface GammaTxnRefFactory extends TxnRefFactory {

    @Override
    <E> GammaTxnRef<E> newTxnRef(E value);

    @Override
    GammaTxnInteger newTxnInteger(int value);

    @Override
    GammaTxnBoolean newTxnBoolean(boolean value);

    @Override
    GammaTxnDouble newTxnDouble(double value);

    @Override
    GammaTxnLong newTxnLong(long value);
}
