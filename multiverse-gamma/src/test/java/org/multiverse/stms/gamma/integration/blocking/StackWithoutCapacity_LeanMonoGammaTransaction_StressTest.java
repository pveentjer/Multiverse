package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransactionFactory;

public class StackWithoutCapacity_LeanMonoGammaTransaction_StressTest extends StackWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected AtomicBlock newPopAtomicBLock() {
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(stm));
    }

    @Override
    protected AtomicBlock newPushAtomicBLock() {
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(stm));
    }
}
