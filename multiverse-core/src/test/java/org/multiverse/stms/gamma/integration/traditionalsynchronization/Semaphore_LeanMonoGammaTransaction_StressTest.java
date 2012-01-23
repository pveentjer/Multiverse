package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransactionFactory;

/**
 * @author Peter Veentjer
 */
public class Semaphore_LeanMonoGammaTransaction_StressTest extends Semaphore_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected AtomicBlock newDownBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm);
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(config));
    }

    @Override
    protected AtomicBlock newUpBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm);
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(config));
    }
}



