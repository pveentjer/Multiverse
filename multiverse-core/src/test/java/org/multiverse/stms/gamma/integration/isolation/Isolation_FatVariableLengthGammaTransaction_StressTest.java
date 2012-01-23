package org.multiverse.stms.gamma.integration.isolation;

import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

public class Isolation_FatVariableLengthGammaTransaction_StressTest extends Isolation_AbstractTest {

    @Override
    protected AtomicBlock newBlock(LockMode lockMode, boolean dirtyCheckEnabled) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode)
                .setDirtyCheckEnabled(dirtyCheckEnabled);
        return new LeanGammaAtomicBlock(new FatVariableLengthGammaTransactionFactory(config));
    }
}
