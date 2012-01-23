package org.multiverse.stms.gamma;

import org.multiverse.api.references.RefFactory;
import org.multiverse.stms.gamma.transactionalobjects.*;

/**
 * A {@link RefFactory} tailored for the GammaStm.
 *
 * @author Peter Veentjer.
 */
public interface GammaRefFactory extends RefFactory {

    @Override
    <E> GammaRef<E> newRef(E value);

    @Override
    GammaIntRef newIntRef(int value);

    @Override
    GammaBooleanRef newBooleanRef(boolean value);

    @Override
    GammaDoubleRef newDoubleRef(double value);

    @Override
    GammaLongRef newLongRef(long value);
}
