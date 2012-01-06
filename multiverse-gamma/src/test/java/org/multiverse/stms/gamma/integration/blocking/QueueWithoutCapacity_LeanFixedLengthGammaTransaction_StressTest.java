package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransactionFactory;

public class QueueWithoutCapacity_LeanFixedLengthGammaTransaction_StressTest extends QueueWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected AtomicBlock newPopBlock() {
        return new LeanGammaAtomicBlock(new LeanFixedLengthGammaTransactionFactory(stm));
    }

    @Override
    protected AtomicBlock newPushBlock() {
        return new LeanGammaAtomicBlock(new LeanFixedLengthGammaTransactionFactory(stm));
    }
}
