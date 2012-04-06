package org.multiverse.stms.gamma.integration.isolation;

import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

public class Isolation_FatVariableLengthGammaTransaction_StressTest extends Isolation_AbstractTest {

    @Override
    protected TransactionExecutor newBlock(LockMode lockMode, boolean dirtyCheckEnabled) {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode)
                .setDirtyCheckEnabled(dirtyCheckEnabled);
        return new LeanGammaTransactionExecutor(new FatVariableLengthGammaTxnFactory(config));
    }
}
