package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransactionFactory;

/**
 * @author Peter Veentjer
 */
public class Semaphore_LeanFixedLengthGammaTransaction_StressTest extends Semaphore_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TransactionExecutor newDownBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm);
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTransactionFactory(config));
    }

    @Override
    protected TransactionExecutor newUpBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm);
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTransactionFactory(config));
    }
}


