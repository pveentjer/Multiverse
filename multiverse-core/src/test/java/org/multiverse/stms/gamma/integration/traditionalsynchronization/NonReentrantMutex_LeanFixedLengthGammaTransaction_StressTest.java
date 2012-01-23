package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransactionFactory;

public class NonReentrantMutex_LeanFixedLengthGammaTransaction_StressTest extends NonReentrantMutex_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected AtomicBlock newLockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaAtomicBlock(new LeanFixedLengthGammaTransactionFactory(config));
    }

    @Override
    protected AtomicBlock newUnlockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaAtomicBlock(new LeanFixedLengthGammaTransactionFactory(config));
    }
}
