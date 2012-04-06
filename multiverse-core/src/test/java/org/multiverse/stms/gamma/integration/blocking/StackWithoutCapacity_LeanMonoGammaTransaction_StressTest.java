package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxnFactory;

public class StackWithoutCapacity_LeanMonoGammaTransaction_StressTest extends StackWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TransactionExecutor newPopTransactionExecutor() {
        return new LeanGammaTransactionExecutor(new LeanMonoGammaTxnFactory(stm));
    }

    @Override
    protected TransactionExecutor newPushTransactionExecutor() {
        return new LeanGammaTransactionExecutor(new LeanMonoGammaTxnFactory(stm));
    }
}
