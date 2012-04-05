package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransactionFactory;

public class NonReentrantMutex_LeanMonoGammaTransaction_StressTest extends NonReentrantMutex_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TransactionExecutor newLockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaTransactionExecutor(new LeanMonoGammaTransactionFactory(config));
    }

    @Override
    protected TransactionExecutor newUnlockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaTransactionExecutor(new LeanMonoGammaTransactionFactory(config));
    }
}
