package org.multiverse.stms.gamma.integration.isolation;

import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;

public class Isolation_FatMonoGammaTransaction_StressTest extends Isolation_AbstractTest {
    @Override
    protected AtomicBlock newBlock(LockMode lockMode, boolean dirtyCheckEnabled) {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode)
                .setDirtyCheckEnabled(dirtyCheckEnabled);
        return new LeanGammaAtomicBlock(new FatMonoGammaTransactionFactory(config));
    }
}
