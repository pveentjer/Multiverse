package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxnFactory;

public class NonReentrantMutex_LeanFixedLengthGammaTxn_StressTest extends NonReentrantMutex_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TxnExecutor newLockBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaxRetries(10000);
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(config));
    }

    @Override
    protected TxnExecutor newUnlockBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaxRetries(10000);
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(config));
    }
}
