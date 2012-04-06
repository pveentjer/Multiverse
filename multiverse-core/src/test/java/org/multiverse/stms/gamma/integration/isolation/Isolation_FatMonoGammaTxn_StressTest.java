package org.multiverse.stms.gamma.integration.isolation;

import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;

public class Isolation_FatMonoGammaTxn_StressTest extends Isolation_AbstractTest {
    @Override
    protected TxnExecutor newBlock(LockMode lockMode, boolean dirtyCheckEnabled) {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode)
                .setDirtyCheckEnabled(dirtyCheckEnabled);
        return new LeanGammaTxnExecutor(new FatMonoGammaTxnFactory(config));
    }
}
