package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransactionFactory;

public class QueueWithoutCapacity_LeanFixedLengthGammaTransaction_StressTest extends QueueWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TransactionExecutor newPopBlock() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTransactionFactory(stm));
    }

    @Override
    protected TransactionExecutor newPushBlock() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTransactionFactory(stm));
    }
}
