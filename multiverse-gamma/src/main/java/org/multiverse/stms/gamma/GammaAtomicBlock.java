package org.multiverse.stms.gamma;

import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;

/**
 * An {@link AtomicBlock} tailored for the GammaStm.
 *
 * @author  Peter Veentjer.
 */
public interface GammaAtomicBlock extends AtomicBlock {

    @Override
    GammaTransactionFactory getTransactionFactory();
}
